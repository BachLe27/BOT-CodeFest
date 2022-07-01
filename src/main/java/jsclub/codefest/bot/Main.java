package jsclub.codefest.bot;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.bot.constant.GameConfig;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static String getRandomPath() {
        Random rand = new Random();
        int random_integer = rand.nextInt(5);

        return "1234b".charAt(random_integer) + "";
    }

    public static String tactic(MapInfo mapInfo) {
        mapInfo.updateMapInfo();
        //set safenode, restrictednode
        //sort safenode by value
        //use astarsearch
        int [][] map = mapInfo.getMap();

        System.out.println("Int array: ");
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + ' ');
            }
            System.out.println();
        }

        return "1";
    }

    public static void main(String[] aDrgs) {
        Hero player1 = new Hero("player1-xxx", GameConfig.GAME_ID);
        Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo = gameInfo.map_info;

            Position currentPosition = mapInfo.getCurrentPosition(player1);
            Position enemyPosisiton = mapInfo.getEnemyPosition(player1);
            List<Position> restrictPosition =  new ArrayList<Position>();

            player1.move(tactic(mapInfo));
        };
        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer();
    }
}
