#!/usr/bin/env python3
import subprocess
import sys
import argparse
from ..config import Config

# ===============================
# Load & validate config
# ===============================
config = Config()

# ===============================
# Workflow classes
# ===============================
class Workflow:
    def __init__(self, config):
        self.verify_step = Verify(config)
        self.test_step = Test(config)
        self.build_step = Build(config)
        self.publish_step = Publish(config)

    def run_verify(self):
        self.verify_step.run()

    def run_test(self):
        self.test_step.run()

    def run_build(self):
        self.build_step.run()

    def run_publish(self):
        self.publish_step.run()

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
        try:
            subprocess.run(["docker", "info"], check=True, stdout=subprocess.DEVNULL)
            print("[VERIFY] Docker is available")
        except subprocess.CalledProcessError:
            print("[ERROR] Docker is not running or accessible")
            sys.exit(1)

    def _files_exist(self):
        missing = [f for f in self.config["required_files"] if not os.path.exists(f)]
        if missing:
            print(f"[ERROR] Missing required files/folders: {missing}")
            sys.exit(1)
        print("[VERIFY] All required files exist")

    def _docker_login_if_needed(self):
        username = os.environ.get("DOCKER_HUB_NAME")
        token = os.environ.get("DOCKER_HUB_TOKEN")
        if username and token:
            subprocess.run(["docker", "login", "--username", username, "--password-stdin"],
                           input=token.encode(), check=True)
            print("[VERIFY] Docker login successful")
        else:
            print("[VERIFY] Docker credentials not set; publish will be skipped")

# -------------------------------
# Test
# -------------------------------
class Test:
    def __init__(self, config):
        self.config = config

    def run(self):
        print("[TEST] Running Kotlin backend unit tests...")
        result = subprocess.run(["./gradlew", "test"])
        if result.returncode != 0:
            print("[ERROR] Tests failed. Aborting workflow.")
            sys.exit(1)
        print("[TEST] All tests passed")

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
        if subprocess.run(["test", "-d", frontend_dir]).returncode == 0:
            print("[BUILD] Building frontend...")
            subprocess.run(["npm", "ci"], cwd=frontend_dir, check=True)
            subprocess.run(["npm", "run", "build"], cwd=frontend_dir, check=True)
            subprocess.run(["cp", "-r", f"{frontend_dir}/dist/", "./src/main/resources/static"], check=True)

    def _build_backend(self):
        print("[BUILD] Building Kotlin backend...")
        subprocess.run(["./gradlew", "build"], check=True)

# -------------------------------
# Publish
# -------------------------------
class Publish:
    def __init__(self, config):
        self.config = config

    def run(self):
        image_name = self.config["docker"]["image_name"]
        tag = self.config["docker"]["tag"]
        dockerfile = self.config["docker"]["dockerfile"]

        print(f"[PUBLISH] Building Docker image {image_name}:{tag} ...")
        subprocess.run(["docker", "build", "-f", dockerfile, "-t", f"{image_name}:{tag}", "."], check=True)

        username = os.environ.get("DOCKER_HUB_NAME")
        token = os.environ.get("DOCKER_HUB_TOKEN")
        if username and token:
            print(f"[PUBLISH] Pushing {image_name}:{tag} ...")
            subprocess.run(["docker", "push", f"{image_name}:{tag}"], check=True)
        else:
            print("[PUBLISH] Docker credentials not set; skipping push")

# ===============================
# CLI
# ===============================
parser = argparse.ArgumentParser(description="Auota Production Workflow")
parser.add_argument("--verify", action="store_true", help="Verify environment and files")
parser.add_argument("--test", action="store_true", help="Run backend unit tests")
parser.add_argument("--build", action="store_true", help="Build frontend and backend")
parser.add_argument("--publish", action="store_true", help="Build and push Docker image")
parser.add_argument("--all", action="store_true", help="Run all steps")
args = parser.parse_args()

wf = Workflow(config)

if args.all:
    wf.run_all()
else:
    if args.verify:
        wf.run_verify()
    if args.test:
        wf.run_test()
    if args.build:
        wf.run_build()
    if args.publish:
        wf.run_publish()