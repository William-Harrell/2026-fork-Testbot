#!/bin/bash
# Orange Pi Vision System Installation Script
# Run this script on the Orange Pi 5 to set up the vision system
# Installs both PhotonVision (AprilTags) and the ML inference service

set -e

echo "=========================================="
echo "Orange Pi Vision System Installer"
echo "=========================================="

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root (sudo ./install.sh)"
    exit 1
fi

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INSTALL_DIR="/opt/orangepi-vision"
PHOTONVISION_DIR="/opt/photonvision"
PHOTONVISION_VERSION="v2025.1.1"

echo ""
echo "Step 1: Installing system dependencies..."
apt-get update
apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    python3-opencv \
    libopencv-dev \
    v4l-utils \
    git \
    openjdk-17-jre-headless \
    wget

echo ""
echo "Step 2: Installing PhotonVision..."
mkdir -p "$PHOTONVISION_DIR"
if [ ! -f "$PHOTONVISION_DIR/photonvision.jar" ]; then
    echo "Downloading PhotonVision ${PHOTONVISION_VERSION}..."
    wget -O "$PHOTONVISION_DIR/photonvision.jar" \
        "https://github.com/PhotonVision/photonvision/releases/download/${PHOTONVISION_VERSION}/photonvision-${PHOTONVISION_VERSION}.jar" || {
        echo "Warning: Could not download PhotonVision. Please download manually."
        echo "Visit: https://github.com/PhotonVision/photonvision/releases"
    }
else
    echo "PhotonVision JAR already present, skipping download"
fi

echo ""
echo "Step 3: Creating ML service installation directory..."
mkdir -p "$INSTALL_DIR"
cp -r "$SCRIPT_DIR"/* "$INSTALL_DIR/"

echo ""
echo "Step 4: Creating Python virtual environment..."
cd "$INSTALL_DIR"
python3 -m venv venv
source venv/bin/activate

echo ""
echo "Step 5: Installing Python dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

echo ""
echo "Step 6: Installing RKNN Toolkit2 Lite..."
# Download and install RKNN Toolkit2 Lite for Orange Pi 5
# Note: You may need to adjust this URL based on the latest release
RKNN_WHL="rknn_toolkit_lite2-1.6.0-cp310-cp310-linux_aarch64.whl"
if [ ! -f "/tmp/$RKNN_WHL" ]; then
    echo "Downloading RKNN Toolkit2 Lite..."
    wget -O "/tmp/$RKNN_WHL" \
        "https://github.com/rockchip-linux/rknn-toolkit2/releases/download/v1.6.0/$RKNN_WHL" || {
        echo "Warning: Could not download RKNN wheel. Please install manually."
        echo "Visit: https://github.com/rockchip-linux/rknn-toolkit2/releases"
    }
fi

if [ -f "/tmp/$RKNN_WHL" ]; then
    pip install "/tmp/$RKNN_WHL"
fi

echo ""
echo "Step 7: Setting up models directory..."
mkdir -p "$INSTALL_DIR/models"
chmod 755 "$INSTALL_DIR/models"

echo ""
echo "Step 8: Installing systemd services..."
cp "$SCRIPT_DIR/systemd/photonvision.service" /etc/systemd/system/
cp "$SCRIPT_DIR/systemd/orangepi-vision.service" /etc/systemd/system/
systemctl daemon-reload
systemctl enable photonvision.service
systemctl enable orangepi-vision.service

echo ""
echo "Step 9: Setting permissions..."
chmod +x "$INSTALL_DIR/vision_service.py"

echo ""
echo "=========================================="
echo "Installation Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Copy your RKNN model files to: $INSTALL_DIR/models/"
echo "   - robot_detector.rknn"
echo "   - fuel_detector.rknn"
echo ""
echo "2. Update configuration in: $INSTALL_DIR/config.py"
echo "   - Set correct roboRIO IP address"
echo "   - Adjust camera stream URLs if needed"
echo ""
echo "3. Configure PhotonVision cameras:"
echo "   - Start PhotonVision: sudo systemctl start photonvision"
echo "   - Open http://<orangepi-ip>:5800 in a browser"
echo "   - Add 3 cameras: front_cam, back_left_cam, back_right_cam"
echo "   - Verify MJPEG streams at ports 1182, 1184, 1186"
echo ""
echo "4. Start both services:"
echo "   sudo systemctl start photonvision"
echo "   sudo systemctl start orangepi-vision"
echo ""
echo "5. View logs:"
echo "   sudo journalctl -u photonvision -f"
echo "   sudo journalctl -u orangepi-vision -f"
echo ""
echo "6. Test ML service manually (optional):"
echo "   cd $INSTALL_DIR && source venv/bin/activate"
echo "   python vision_service.py --simulate"
echo ""
