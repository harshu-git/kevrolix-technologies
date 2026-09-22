import os
import sys
import shutil
import zipfile
import subprocess
import urllib.request

ROOT_DIR = os.path.abspath(os.path.dirname(__file__))
TOOLS_DIR = os.path.join(ROOT_DIR, "tools")
JDK_DIR = os.path.join(TOOLS_DIR, "jdk-17")
SDK_DIR = os.path.join(TOOLS_DIR, "android-sdk")

JDK_URL = "https://download.visualstudio.microsoft.com/download/pr/58dca6c6-c3c9-4daa-8e17-b7d0df501afc/667a0e2c93e1aab8df0707a31c42d190/microsoft-jdk-17.0.12-windows-x64.zip"
CMDLINE_TOOLS_URL = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"

def download_with_progress(url, dest_path, desc):
    if os.path.exists(dest_path):
        print(f"[{desc}] Already exists at {dest_path}")
        return
    print(f"[{desc}] Downloading from {url}...")
    headers = {'User-Agent': 'Mozilla/5.0'}
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req) as response, open(dest_path, 'wb') as out_file:
        total_size = int(response.headers.get('Content-Length', 0))
        block_size = 1024 * 1024
        downloaded = 0
        while True:
            buffer = response.read(block_size)
            if not buffer:
                break
            downloaded += len(buffer)
            out_file.write(buffer)
            if total_size > 0:
                percent = downloaded * 100 // total_size
                mb = downloaded / (1024 * 1024)
                total_mb = total_size / (1024 * 1024)
                sys.stdout.write(f"\r[{desc}] {mb:.1f}/{total_mb:.1f} MB ({percent}%)")
                sys.stdout.flush()
    print(f"\n[{desc}] Download complete.")

def setup_jdk():
    os.makedirs(TOOLS_DIR, exist_ok=True)
    java_exe = os.path.join(JDK_DIR, "bin", "java.exe")
    if os.path.exists(java_exe):
        print("[JDK] Found existing OpenJDK 17 installation.")
        return JDK_DIR

    jdk_zip = os.path.join(TOOLS_DIR, "jdk17.zip")
    download_with_progress(JDK_URL, jdk_zip, "OpenJDK 17")

    print("[JDK] Extracting OpenJDK 17...")
    extract_temp = os.path.join(TOOLS_DIR, "jdk_temp")
    os.makedirs(extract_temp, exist_ok=True)
    with zipfile.ZipFile(jdk_zip, 'r') as z:
        z.extractall(extract_temp)

    # Move inner jdk directory to JDK_DIR
    subdirs = [d for d in os.listdir(extract_temp) if os.path.isdir(os.path.join(extract_temp, d))]
    if subdirs:
        inner = os.path.join(extract_temp, subdirs[0])
        if os.path.exists(JDK_DIR):
            shutil.rmtree(JDK_DIR)
        shutil.move(inner, JDK_DIR)
    shutil.rmtree(extract_temp, ignore_errors=True)
    if os.path.exists(jdk_zip):
        os.remove(jdk_zip)
    print(f"[JDK] OpenJDK 17 ready at {JDK_DIR}")
    return JDK_DIR

def setup_android_sdk():
    cmdline_latest = os.path.join(SDK_DIR, "cmdline-tools", "latest", "bin", "sdkmanager.bat")
    if os.path.exists(cmdline_latest):
        print("[SDK] Found existing Android command-line tools.")
        return SDK_DIR

    os.makedirs(SDK_DIR, exist_ok=True)
    cmdline_zip = os.path.join(TOOLS_DIR, "cmdline-tools.zip")
    download_with_progress(CMDLINE_TOOLS_URL, cmdline_zip, "Android Cmdline Tools")

    print("[SDK] Extracting command-line tools...")
    target_latest = os.path.join(SDK_DIR, "cmdline-tools", "latest")
    os.makedirs(target_latest, exist_ok=True)
    extract_temp = os.path.join(TOOLS_DIR, "cmdline_temp")
    os.makedirs(extract_temp, exist_ok=True)

    with zipfile.ZipFile(cmdline_zip, 'r') as z:
        z.extractall(extract_temp)

    # cmdline-tools is unpacked as cmdline_temp/cmdline-tools/*
    src_cmdline = os.path.join(extract_temp, "cmdline-tools")
    if os.path.exists(src_cmdline):
        for item in os.listdir(src_cmdline):
            s = os.path.join(src_cmdline, item)
            d = os.path.join(target_latest, item)
            if os.path.exists(d):
                if os.path.isdir(d):
                    shutil.rmtree(d)
                else:
                    os.remove(d)
            shutil.move(s, d)

    shutil.rmtree(extract_temp, ignore_errors=True)
    if os.path.exists(cmdline_zip):
        os.remove(cmdline_zip)
    print(f"[SDK] Android Cmdline Tools ready at {target_latest}")
    return SDK_DIR

def accept_licenses():
    licenses_dir = os.path.join(SDK_DIR, "licenses")
    os.makedirs(licenses_dir, exist_ok=True)
    
    # Official accepted license hashes
    hashes = {
        "android-sdk-license": "\n24333f8a63b682569279e447f733e80164042c72\n8933bad161af4178b1185d1a37fbf41ea5269c55\nd56f5187479451eabf01fb78af6dfcb131a6481e\n",
        "android-sdk-preview-license": "\n84831b9409646a918e30573bab4c9c91346d8abd\n",
        "intel-android-extra-license": "\nd975f751698a77b662f1254ddbeed3901e976f5a\n"
    }
    for fname, content in hashes.items():
        with open(os.path.join(licenses_dir, fname), "w") as f:
            f.write(content)
    print("[SDK] Pre-accepted Android SDK licenses.")

def write_local_properties():
    sdk_escaped = SDK_DIR.replace("\\", "/")
    props_path = os.path.join(ROOT_DIR, "local.properties")
    with open(props_path, "w") as f:
        f.write(f"sdk.dir={sdk_escaped}\n")
    print(f"[Config] Written local.properties (sdk.dir={sdk_escaped})")

def install_platform_and_build_tools(jdk_path):
    platform_dir = os.path.join(SDK_DIR, "platforms", "android-35")
    build_tools_dir = os.path.join(SDK_DIR, "build-tools", "35.0.0")
    if os.path.exists(platform_dir) and os.path.exists(build_tools_dir):
        print("[SDK] Platform 35 and Build Tools 35.0.0 are already installed.")
        return

    sdkmanager = os.path.join(SDK_DIR, "cmdline-tools", "latest", "bin", "sdkmanager.bat")
    env = os.environ.copy()
    env["JAVA_HOME"] = jdk_path
    env["ANDROID_HOME"] = SDK_DIR
    env["PATH"] = f"{os.path.join(jdk_path, 'bin')};{env.get('PATH', '')}"

    print("[SDK] Installing platforms;android-35 and build-tools;35.0.0 via sdkmanager...")
    cmd = [sdkmanager, "--install", "platforms;android-35", "build-tools;35.0.0"]
    proc = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, env=env)
    stdout, _ = proc.communicate(input="y\ny\ny\n")
    print(stdout[-500:] if len(stdout) > 500 else stdout)
    if proc.returncode != 0:
        print(f"[SDK] Warning: sdkmanager exited with code {proc.returncode}")
    else:
        print("[SDK] Platforms and Build Tools installed successfully.")

def build_gradle(jdk_path):
    gradlew = os.path.join(ROOT_DIR, "gradlew.bat")
    env = os.environ.copy()
    env["JAVA_HOME"] = jdk_path
    env["ANDROID_HOME"] = SDK_DIR
    env["PATH"] = f"{os.path.join(jdk_path, 'bin')};{env.get('PATH', '')}"

    tasks = sys.argv[1:] if len(sys.argv) > 1 else ["bundleRelease", "assembleRelease"]
    print("\n" + "="*60)
    print(f"STARTING GRADLE BUILD: {' '.join(tasks)}")
    print("="*60 + "\n")

    cmd = [gradlew] + tasks + ["--stacktrace"]
    proc = subprocess.Popen(cmd, cwd=ROOT_DIR, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    
    for line in proc.stdout:
        sys.stdout.write(line)
        sys.stdout.flush()

    proc.wait()
    if proc.returncode != 0:
        print(f"\n[ERROR] Gradle build failed with code {proc.returncode}")
        sys.exit(proc.returncode)

    print("\n" + "="*60)
    print("BUILD SUCCESSFUL!")
    print("="*60)

    out_dir = os.path.join(ROOT_DIR, "build-output")
    os.makedirs(out_dir, exist_ok=True)

    # 1. Locate Release AAB
    aab_source = os.path.join(ROOT_DIR, "app", "build", "outputs", "bundle", "release", "app-release.aab")
    dest_aab = os.path.join(out_dir, "AntiPhoneSnatcher-release.aab")
    if os.path.exists(aab_source):
        shutil.copyfile(aab_source, dest_aab)
        size_mb = os.path.getsize(dest_aab) / (1024 * 1024)
        print(f"\n[Google Play App Bundle] Ready for Store Upload:\n  {dest_aab} ({size_mb:.2f} MB)")

    # 2. Locate Release APK
    apk_release = os.path.join(ROOT_DIR, "app", "build", "outputs", "apk", "release", "app-release.apk")
    dest_apk_release = os.path.join(out_dir, "AntiPhoneSnatcher-release.apk")
    if os.path.exists(apk_release):
        shutil.copyfile(apk_release, dest_apk_release)
        size_mb = os.path.getsize(dest_apk_release) / (1024 * 1024)
        print(f"\n[Release APK] Signed Production APK:\n  {dest_apk_release} ({size_mb:.2f} MB)")

    # 3. Locate Debug APK if built
    apk_debug = os.path.join(ROOT_DIR, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
    dest_apk_debug = os.path.join(out_dir, "AntiPhoneSnatcher-debug.apk")
    if os.path.exists(apk_debug):
        shutil.copyfile(apk_debug, dest_apk_debug)
        size_mb = os.path.getsize(dest_apk_debug) / (1024 * 1024)
        print(f"\n[Debug APK]:\n  {dest_apk_debug} ({size_mb:.2f} MB)")

def main():
    jdk_path = setup_jdk()
    setup_android_sdk()
    accept_licenses()
    write_local_properties()
    install_platform_and_build_tools(jdk_path)
    build_gradle(jdk_path)

if __name__ == "__main__":
    main()
