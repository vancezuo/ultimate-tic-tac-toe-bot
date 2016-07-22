package bot;

import theaigames.BotParser;
import theaigames.BotStarter;

/**
 * @author Vance Zuo
 * Created: 7/22/2016
 */
public class Main {
    public static void main(String[] args) {
        BotParser parser = new BotParser(new BotStarter());
        parser.run();
    }
}
