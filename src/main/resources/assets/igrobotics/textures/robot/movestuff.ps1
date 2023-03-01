
$targDir = "C:\Users\Nathan\Desktop\MOOOOODS\Minecraft\Modding\New Workspaces\Robotics\src\main\resources\assets\igrobotics\textures\robot\color\";
$subDir = $(Get-ChildItem "$targDir");


foreach($sub in $subDir) {
	cd "$targDir$sub"
	mv "tmp.png" "left_arm.png"
	cd ..
}