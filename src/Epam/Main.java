package Epam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
	// write your code here
        System.out.println("Hello there, this is my console!");
        System.out.println("Please, type in a default path (Press Enter to set default directory to C:\\)");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Thread console;
        try {
            String tmp = reader.readLine();
            if (!tmp.equals("")) {
                console = new Thread(new Console(tmp));
            }
            else {
                console = new Thread(new Console());
            }
            console.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
