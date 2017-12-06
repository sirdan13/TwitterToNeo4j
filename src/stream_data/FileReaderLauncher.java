package stream_data;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import utilities.Launcher;

public class FileReaderLauncher {

	public static void main(String[] args) {
		int contatore = 0;
		while(contatore<10){
			try {
	            Runtime.getRuntime().exec("cmd /c start cmd.exe /K \" cd C:\\Users\\daniele\\Desktop\\Tesi\\Eseguibili && java -jar FileReader.jar "+contatore);
	            contatore++;
	        } catch (IOException ex) {
	            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
	        }
		}
		

	}

}
