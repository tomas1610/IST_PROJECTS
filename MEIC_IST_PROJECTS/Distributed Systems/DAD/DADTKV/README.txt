In this file are the instructions on how to run the DADTKV project in this folder.

INSTRUCTIONS:

Open the solution on Visual Studio

Go to Manager -> Properties -> Debug 
	1. Click on "Open debug launch profiles UI"
	2. In "Command line arguments" input the following text "../Scripts/configuration_sample.txt" (with the "")
	3. Change the Working Directory to "YOUR_PROJECT_PATH_HERE\Manager"
	4. Change line 292 of Program.cs of the Manager with your local path

Should be ready to run!

IMPORTANT: Sometimes the process cant be started with the message: "Unable to load because it is being used by another process"
	   Retry running.

	