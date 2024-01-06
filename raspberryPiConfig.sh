cat >>/home/florian/.config/wayfire.ini <<'WAYFIREINIEOF'

[output:DSI-1]
mode = 800x480@60049
position = 0,0
transform = 180
  
[autostart]
background = wfrespawn pcmanfm --desktop --profile LXDE-pi
chromium = chromium-browser  https://touraine.tech/timer --kiosk --noerrdialogs --disable-infobars --no-first-run --ozone-platform=wayland --enable-features=OverlayScrollbar --start-maximized
screensaver = false
dpms = false
WAYFIREINIEOF
