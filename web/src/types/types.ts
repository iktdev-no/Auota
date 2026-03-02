// AUTO-GENERATED. DO NOT EDIT.
// Source: no.iktdev.auota.models

export interface TransferSelectionCount {
  Bytes: number | null;
  Files: number | null;
}

export type DecryptionState = "NOT_INITIALIZED" | "INITIALIZING" | "RESTORING" | "READY" | "FAILED" | "REJECTED" | "NOT_ENABLED" | "TEARDOWN" | "MANUAL_OVERRIDE"

export type EncryptionState = "NOT_INITIALIZED" | "INITIALIZING" | "RESTORING" | "READY" | "FAILED" | "REJECTED" | "NOT_ENABLED" | "TEARDOWN" | "MANUAL_OVERRIDE"

export interface BackupInfo {
  State: BackupState | null;
}

export interface CryptConfig {
  algorithm: string;
  enabled: boolean;
  password: string | null;
}

export interface GlobalState {
  Downloading: Record<string, any> | null;
  LastTokenRefresh: number | null;
  RestoreWorking: boolean | null;
  Uploading: Record<string, any> | null;
}

export interface JottaTransfer {
  CompletedTimeMs: number | null;
  Errors: TransferErrors | null;
  Id: string;
  Local: string;
  Remaining: TransferRemaining | null;
  Remote: string;
  SelectionCount: TransferSelectionCount | null;
  StartedTimeMs: number | null;
  Total: TransferTotal;
}

export interface OperationRequest {
  backupPath: string | null;
  duration: string | null;
}

export interface UserInfo {
  AccountInfo: AccountInfo | null;
  Avatar: AvatarInfo | null;
  Brand: string | null;
  Email: string | null;
  Fullname: string | null;
  Hostname: string | null;
  device: DeviceInfo | null;
}

export type AuthStatus = "LOGGED_IN" | "LOGGED_OUT" | "UNKNOWN"

export interface BackupFolder {
  Count: Record<string, any> | null;
  DeviceID: string | null;
  ErrorFilesCount: Record<string, any> | null;
  ErrorFoldersCount: number | null;
  Errors: Record<string, any> | null;
  History: BackupHistory[] | null;
  LastScanStartedMS: number | null;
  LastUpdateMS: number | null;
  Name: string | null;
  NextBackupMS: number | null;
  Path: string | null;
  Uploading: Record<string, any> | null;
}

export interface UploadHistory {
  Completed: Record<string, any> | null;
  Started: Record<string, any> | null;
}

export interface JottaVersionInfo {
  jottaCliVersion: string;
  jottadAppdata: string;
  jottadExecutable: string;
  jottadLogfile: string;
  jottadVersion: string;
  releaseNotes: string;
  remoteVersion: string;
}

export interface AvatarInfo {
  Background: RgbColor | null;
  Initials: string | null;
}

export interface BackupState {
  Enabled: EnabledBackup | null;
}

export interface DeviceInfo {
  Name: string | null;
  Type: number | null;
}

export interface GocryptfsConfigExport {
  base64Config: string;
  sha256: string;
}

export interface SystemHealth {
  auth: AuthStatus;
  backendExists: boolean;
  encryption: EncryptionState;
  jottad: JottaDaemonState;
  lastUpdated: number;
  mounted: boolean;
}

export interface BackupHistory {
  Ended: number | null;
  Finished: boolean | null;
  Path: string | null;
  Started: number | null;
  Total: Record<string, any> | null;
  Upload: UploadHistory | null;
}

export interface TransferTotal {
  Bytes: number | null;
  Files: number | null;
}

export interface JottadStatus {
  alive: boolean;
  pid: number;
  state: JottaDaemonState;
  timestamp: number;
}

export interface JottaStatus {
  Backup: BackupInfo | null;
  State: GlobalState | null;
  Sync: SyncInfo | null;
  User: UserInfo | null;
}

export type FileType = "Folder" | "File"

export type JottaFsItem = JottaFile | JottaFolder

export interface File {
  type: "File";
  actions: FileAction[];
  created: number;
  extension: string;
  icon: FileIcon;
  isDataSource: boolean;
  isEncrypted: boolean;
  isExcludedFromBackup: boolean;
  isInBackup: boolean;
  name: string;
  size: number;
  uri: string;
}

export type RootType = "Jotta" | "UploadUnencrypted" | "UploadEncrypted" | "Download" | "LocalFolder"

export type IFile = File | Folder

export interface FileAction {
  id: FileActionType;
  requiresConfirmation: boolean;
  title: string;
}

export type FileIcon = "Default" | "Encrypted" | "Backend" | "BackupIncluded" | "BackupExcluded"

export interface Roots {
  id: string;
  name: string;
  path: string;
  type: RootType;
}

export interface Folder {
  type: "Folder";
  actions: FileAction[];
  created: number;
  icon: FileIcon;
  isDataSource: boolean;
  isEncrypted: boolean;
  isExcludedFromBackup: boolean;
  isInBackup: boolean;
  name: string;
  uri: string;
}

export interface JottaFs {
  Files: JottaFile[] | null;
  Folders: JottaFolder[] | null;
}

export interface JottaFile {
  type: "JottaFile";
  Checksum: string;
  Modified: number;
  Name: string;
  Path: string;
  Size: number;
  actions: FileAction[];
  extension: string;
}

export type FileActionType = "AddToBackup" | "IncludeInBackup" | "ExcludeFromBackup" | "RemoveFromBackup" | "Upload" | "Download" | "Open"

export interface JottaFolder {
  type: "JottaFolder";
  Name: string;
  Path: string | null;
  actions: FileAction[];
}

export interface LogfileResponse {
  message: string | null;
  path: string | null;
  success: boolean;
}

export interface JottaSummary {
  message: string | null;
  parsed: JottaStatus | null;
  raw: string;
  success: boolean;
}

export interface EnabledBackup {
  Backups: BackupFolder[] | null;
  deviceName: string | null;
}

export interface JottaConfig {
  backuppaused: boolean;
  checksumreadrate: string;
  downloadrate: string;
  ignorehiddenfiles: boolean;
  logscanignores: boolean;
  logtransfers: boolean;
  maxdownloads: number;
  maxuploads: number;
  proxy: string;
  scaninterval: string;
  syncpaused: boolean;
  timeformat: string;
  uploadrate: string;
  usesiunits: boolean;
  webhookstatusinterval: string;
}

export interface BackupItem {
  excludePaths: string[];
  path: string;
}

export interface BackupIEUpdate {
  backupRoot: string | null;
  exclude: string;
}

export interface SseEnvelope {
  payload: any | null;
  type: string;
}

export interface RgbColor {
  b: number | null;
  g: number | null;
  r: number | null;
}

export interface SyncInfo {
  Count: Record<string, any> | null;
  RemoteCount: Record<string, any> | null;
}

export interface OperationResponse {
  message: string;
  raw: string | null;
  success: boolean;
}

export type JottaDaemonState = "NOT_STARTED" | "STARTING" | "RUNNING" | "STOPPED" | "FAILED"

export interface TransferErrors {
  message: string | null;
}

export interface TransferRemaining {
  Bytes: number | null;
  Files: number | null;
}

export interface AuthResponse {
  message: string;
  sessionId: string | null;
  step: AuthStep;
  success: boolean;
}

export interface AccountInfo {
  Capacity: number | null;
  ProductNameLocalized: string | null;
  Subscription: number | null;
  SubscriptionNameLocalized: string | null;
  Usage: number | null;
}

export interface EncryptionStatus {
  algorithm: string | null;
  backendExists: boolean;
  enabled: boolean;
  exportable: boolean;
  manualOverride: boolean;
  mounted: boolean;
  passwordIncorrect: boolean;
  passwordSet: boolean;
  reason: string | null;
  state: EncryptionState;
  verified: boolean;
}

export type AuthStep = "LICENSE" | "PAT" | "WAIT" | "DEVICE_NAME" | "DONE" | "ALREADY_AUTHED" | "CONFIRM" | "ERROR" | "CANCELLED" | "UNKNOWN"

