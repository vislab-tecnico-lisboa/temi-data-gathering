TEMI_IP_ADDRESS=192.168.0.27
TEMI_PORT=5555
PASS=
echo "connecting (only one user allowed at a time"
curl -k "https://$TEMI_IP_ADDRESS:$TEMI_PORT/grantAuth?pwd=$PASS"
echo "connecting"
adb disconnect
/usr/lib/android-sdk/platform-tools/adb connect $TEMI_IP_ADDRESS
echo "start-server"
/usr/lib/android-sdk/platform-tools/adb start-server
adb devices

