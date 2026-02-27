#!/usr/bin/env python3
import subprocess
import sys
import argparse
import logging
from ..config import Config

# ===============================
# Logging setup med farger
# ===============================
class ColoredFormatter(logging.Formatter):
    COLORS = {
        "DEBUG": "\033[94m",
        "INFO": "\033[92m",
        "WARNING": "\033[93m",
        "ERROR": "\033[91m",
        "CRITICAL": "\033[41m",
    }
    RESET = "\033[0m"

    def format(self, record):
        color = self.COLORS.get(record.levelname, self.RESET)
        record.levelname = f"{color}{record.levelname}{self.RESET}"
        return super().format(record)

logger = logging.getLogger("workflow")
handler = logging.StreamHandler()
formatter = ColoredFormatter("[%(levelname)s] %(message)s")
handler.setFormatter(formatter)
logger.addHandler(handler)
logger.setLevel(logging.INFO)

# ===============================
# Helper for kjøring av prosesser
# ===============================
def run_cmd(cmd, cwd=None, capture_output=False):
    """Kjør kommando og håndter feil, return stdout hvis ønsket"""
    logger.debug(f"Kjører: {' '.join(cmd)} (cwd={cwd})")
    try:
        result = subprocess.run(
            cmd,
            cwd=cwd,
            check=True,
            capture_output=capture_output,
            text=True
        )
        return result.stdout if capture_output else None
    except subprocess.CalledProcessError as e:
        logger.error(f"Kommando feilet: {' '.join(cmd)}")
        if e.stdout:
            logger.error(f"stdout: {e.stdout}")
        if e.stderr:
            logger.error(f"stderr: {e.stderr}")
        sys.exit(1)

# ===============================
# Load & validate config
# ===============================
config = Config()

# ===============================
# Workflow classes
# ===============================
class Workflow:
    def __init__(self, config):
        self.config = config
        self.verify_step = Verify(config)
        self.test_step = Test(config)
        self.build_step = Build(config)
        self.publish_step = Publish(config)

    def run_verify(self): self.verify_step.run()
    def run_test(self): self.test_step.run()
    def run_build(self): self.build_step.run()
    def run_publish(self): self.publish_step.run()
    def run_all(self):
        self.run_verify()
        self.run_test()
        self.run_build()
        self.run_publish()

# -------------------------------
# Verify
# -------------------------------
class Verify:
    def __init__(self, config):
        self.config = config

    def run(self):
        self._docker_available()
        self._files_exist()
        self._docker_login_if_needed()

    def _docker_available(self):
        run_cmd(["docker", "info"])
        logger.info("Docker is available")

    def _files_exist(self):
        missing = [f for f in self.config["required_files"] if not os.path.exists(f)]
        if missing:
            logger.critical(f"Missing required files/folders: {missing}")
            sys.exit(1)
        logger.info("All required files exist")

    def _docker_login_if_needed(self):
        username = os.environ.get("DOCKER_HUB_NAME")
        token = os.environ.get("DOCKER_HUB_TOKEN")
        if username and token:
            logger.info("Logging in to Docker Hub")
            run_cmd(["docker", "login", "--username", username, "--password-stdin"], capture_output=False, cwd=None)
        else:
            logger.warning("Docker credentials not set; publish will be skipped")

# -------------------------------
# Test
# -------------------------------
class Test:
    def __init__(self, config):
        self.config = config

    def run(self):
        logger.info("Running Kotlin backend unit tests")
        run_cmd(["./gradlew", "test"])
        logger.info("All tests passed")

# -------------------------------
# Build
# -------------------------------
class Build:
    def __init__(self, config):
        self.config = config

    def run(self):
        self._build_frontend()
        self._build_backend()

    def _build_frontend(self):
        frontend_dir = self.config["frontend_dir"]
        if os.path.isdir(frontend_dir):
            logger.info("Building frontend")
            run_cmd(["npm", "ci"], cwd=frontend_dir)
            run_cmd(["npm", "run", "build"], cwd=frontend_dir)
            run_cmd(["cp", "-r", f"{frontend_dir}/dist/", "./src/main/resources/static"])
        else:
            logger.warning(f"No frontend dir found at {frontend_dir}, skipping frontend build")

    def _build_backend(self):
        logger.info("Building Kotlin backend")
        run_cmd(["./gradlew", "build"])

# -------------------------------
# Publish
# -------------------------------
class Publish:
    def __init__(self, config):
        self.config = config

    def run(self):
        docker_config = self.config["docker"]
        logger.info(f"Building Docker image {docker_config['image_name']}:{docker_config['tag']}")
        run_cmd(["docker", "build", "-f", docker_config["dockerfile"], "-t",
                 f"{docker_config['image_name']}:{docker_config['tag']}", "."])

        username = os.environ.get("DOCKER_HUB_NAME")
        token = os.environ.get("DOCKER_HUB_TOKEN")
        if username and token:
            logger.info(f"Pushing Docker image {docker_config['image_name']}:{docker_config['tag']}")
            run_cmd(["docker", "push", f"{docker_config['image_name']}:{docker_config['tag']}"])
        else:
            logger.warning("Docker credentials not set; skipping push")

# ===============================
# CLI
# ===============================
parser = argparse.ArgumentParser(description="Auota Production Workflow")
parser.add_argument("--verify", action="store_true")
parser.add_argument("--test", action="store_true")
parser.add_argument("--build", action="store_true")
parser.add_argument("--publish", action="store_true")
parser.add_argument("--all", action="store_true")
args = parser.parse_args()

wf = Workflow(config)

if args.all:
    wf.run_all()
else:
    if args.verify: wf.run_verify()
    if args.test: wf.run_test()
    if args.build: wf.run_build()
    if args.publish: wf.run_publish()