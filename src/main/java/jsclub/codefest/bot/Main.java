package jsclub.codefest.bot;

import io.socket.emitter.Emitter.Listener;
import jsclub.codefest.bot.constant.GameConfig;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.algorithm.*;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.*;

public class Main {
    public static String getRandomPath() {
        Random rand = new Random();
        int random_integer = rand.nextInt(5);

        return "1234b".charAt(random_integer) + "";
    }
    private static Map<Node, Stack<Node>> sortByComparator(Map<Node, Stack<Node>> unsortedMap, boolean isReverse) {
        List<Map.Entry<Node, Stack<Node>>> list = new LinkedList<>(unsortedMap.entrySet());
        list.sort((o1, o2) -> {
            double o1Path = o1.getValue().size() / o1.getKey().getV();
            double o2Path = o2.getValue().size() / o2.getKey().getV();
            return Double.compare(o1Path, o2Path);
        });
        if (isReverse) {
            Collections.reverse(list);
        }
        Map<Node, Stack<Node>> listFood = new LinkedHashMap<>();
        for (Map.Entry<Node, Stack<Node>> entry : list) {
            Node food = new Node(entry.getKey().getX(), entry.getKey().getY());
            listFood.put(food, entry.getValue());
        }
        return listFood;
    }
    public static List<Node> getPlacingBom(Node targetBox, boolean isNearPlaceBomb, MapInfo mapInfo,Hero hero) {
        int bombPower=0;
        if (hero != null) {
            for (Player player : mapInfo.players) {
                if (hero.getPlayerName().startsWith(player.id)) {
                    bombPower = player.power;
                    break;
                }
            }
        }
        List<Node> restrictedNode=new ArrayList<>();
        restrictedNode.addAll(mapInfo.getVirussList());
        restrictedNode.addAll(mapInfo.getBalks());
        restrictedNode.addAll(mapInfo.getBombList());
        restrictedNode.addAll(mapInfo.getDHumanList());
        restrictedNode.addAll(mapInfo.getQuarantines());
        restrictedNode.addAll(mapInfo.getTeleportGates());
        restrictedNode.addAll(mapInfo.getWalls());
        List<Node> placeBomb = new ArrayList<>();
        if (isNearPlaceBomb) {
            int checkColMin = targetBox.getX() - 1;
            int checkRowsMin = targetBox.getY() - 1;
            for (int i = checkColMin; i <= targetBox.getX() + 1; i++) {
                for (int j = checkRowsMin; j <= targetBox.getY() + 1; j++) {
                    Node n = new Node(i, j);
                    if (!restrictedNode.contains(n) && !mapInfo.getBalks().contains(n)
                            && (n.getX() == targetBox.getX() || n.getY() == targetBox.getY())) {
                        placeBomb.add(n);
                    }
                }
            }
        } else {
            //Add left bom place
            List<Node> tempPlace = new ArrayList<>();
            for (int i = 1; i <= bombPower; i++) {
                Node leftAffectNode = new Node(targetBox.getX() - i, targetBox.getY());
                if (mapInfo.getWalls().contains(leftAffectNode) || mapInfo.getBalks().contains(leftAffectNode)) {
                    break;
                }
                tempPlace.add(leftAffectNode);

            }
            placeBomb.addAll(tempPlace);

            //Add right bom place
            tempPlace.clear();
            for (int i = 1; i <= bombPower; i++) {
                Node rightAffectNode = new Node(targetBox.getX() + i, targetBox.getY());
                if (mapInfo.getWalls().contains(rightAffectNode) || mapInfo.getBalks().contains(rightAffectNode)) {
                    break;
                }
                tempPlace.add(rightAffectNode);

            }
            placeBomb.addAll(tempPlace);

            //Add up bom place
            tempPlace.clear();
            for (int i = 1; i <= bombPower; i++) {
                Node upAffectNode = new Node(targetBox.getX(), targetBox.getY() - i);
                if (mapInfo.getWalls().contains(upAffectNode) || mapInfo.getBalks().contains(upAffectNode)) {
                    break;
                }
                tempPlace.add(upAffectNode);
            }
            placeBomb.addAll(tempPlace);

            //Add down bom place
            tempPlace.clear();
            for (int i = 1; i <= bombPower; i++) {
                Node downAffectNode = new Node(targetBox.getX(), targetBox.getY() + i);
                if (mapInfo.getWalls().contains(downAffectNode) || mapInfo.getBalks().contains(downAffectNode)) {
                    break;
                }
                tempPlace.add(downAffectNode);
            }
            placeBomb.addAll(tempPlace);
        }
        return placeBomb;
    }
    public static String getSimpleBombPath(List<Node> listPlaceBom, MapInfo mapInfo,Hero hero) {
        List<Node> safeNode=new ArrayList<>();
        int [][] map = mapInfo.getMap();
        safeNode.addAll(mapInfo.getNHumanList());
        safeNode.addAll(mapInfo.getSpoilsList());
        List<Node> restrictedNode=new ArrayList<>();
        restrictedNode.addAll(mapInfo.getVirussList());
        restrictedNode.addAll(mapInfo.getBalks());
        restrictedNode.addAll(mapInfo.getBombList());
        restrictedNode.addAll(mapInfo.getDHumanList());
        restrictedNode.addAll(mapInfo.getQuarantines());
        restrictedNode.addAll(mapInfo.getTeleportGates());
        restrictedNode.addAll(mapInfo.getWalls());
        List<Node> blankSpace=mapInfo.getBlanks();
        blankSpace.removeAll(safeNode);
        blankSpace.removeAll(restrictedNode);
        Node startNode=Node.createFromPosition(mapInfo.getCurrentPosition(hero));
        if (!listPlaceBom.isEmpty()) {
            Map<Node, Stack<Node>> pathToPlacingBom = sortByComparator(AStarSearch.getPathsToAllFoods(mapInfo,hero, listPlaceBom, false), false);
            for (Map.Entry<Node, Stack<Node>> pathPlace : pathToPlacingBom.entrySet()) {
                Node shortestBomPlace = pathPlace.getKey();
                long searchTime = System.currentTimeMillis();
                Stack<Node> step  = AStarSearch.aStarSearch(map,restrictedNode,startNode,shortestBomPlace);
                String steps=BaseAlgorithm.getStepsInString(startNode, step);
                //String steps = algorithm.aStarSearch(cloneBommer, shortestBomPlace, -1);
//                Logger.println("#filterPath: steps = " + steps + ", food = "
//                        + pathPlace.getKey() + "- time = " + (System.currentTimeMillis() - searchTime));
                Node virtualBomplace = shortestBomPlace;
                String escapePath = getEscapePathVirtual( mapInfo, hero,-1);
                if (!steps.isEmpty()) {
                    if (!escapePath.isEmpty()) {
                        return steps;
                    }
                } else {
                    if (Node.createFromPosition(mapInfo.getCurrentPosition(hero)).equals(virtualBomplace)) {
                        if (!escapePath.isEmpty()) {
                            String dropBomb = Dir.DROP_BOMB;
                            dropBomb = dropBomb + escapePath;
                            return dropBomb;
                        }
                    }
                }
            }

        }
        return Dir.INVALID;
    }
    public static String getEscapePathVirtual(MapInfo mapInfo,Hero hero, int numStep) {
        List<Node> safeNode=new ArrayList<>();
        int [][] map = mapInfo.getMap();
        safeNode.addAll(mapInfo.getNHumanList());
        safeNode.addAll(mapInfo.getSpoilsList());
        List<Node> restrictedNode=new ArrayList<>();
        restrictedNode.addAll(mapInfo.getVirussList());
        restrictedNode.addAll(mapInfo.getBalks());
        restrictedNode.addAll(mapInfo.getBombList());
        restrictedNode.addAll(mapInfo.getDHumanList());
        restrictedNode.addAll(mapInfo.getQuarantines());
        restrictedNode.addAll(mapInfo.getTeleportGates());
        restrictedNode.addAll(mapInfo.getWalls());
        List<Node> blankSpace=mapInfo.getBlanks();
        blankSpace.removeAll(safeNode);
        blankSpace.removeAll(restrictedNode);
        Node startNode=Node.createFromPosition(mapInfo.getCurrentPosition(hero));
        Map<Node, Stack<Node>> pathToAllSafePlace = sortByComparator(AStarSearch.getPathsToAllFoods(mapInfo,hero, blankSpace, false), false);
        if (!pathToAllSafePlace.isEmpty()) {
            for (Map.Entry<Node, Stack<Node>> path : pathToAllSafePlace.entrySet()) {
//                long searchTime = System.currentTimeMillis();
                //         AStarSearch.aStarSearch(map, restrictedNode, startNode, path.getKey());

                Stack<Node> step  = AStarSearch.aStarSearch(map,restrictedNode,startNode,path.getKey());
                 String steps=BaseAlgorithm.getStepsInString(startNode, step);
                //String steps = algorithm.aStarSearch(cloneBommer, path.getKey(), -1);
//                Logger.println("#filterPath: steps = " + steps + ", food = "
//                        + path.getKey() + "- time = " + (System.currentTimeMillis() - searchTime));
                if (!steps.isEmpty()) {
                    return steps;
                }
            }
        }
        return Dir.INVALID;
    }
    public static String tactic(MapInfo mapInfo,Hero hero) {
        mapInfo.updateMapInfo();
        //set safenode, restrictednode
        //sort safenode by value
        //use astarsearch
        int [][] map = mapInfo.getMap();
        List<Node> safeNode=new ArrayList<>();

        safeNode.addAll(mapInfo.getNHumanList());
        safeNode.addAll(mapInfo.getSpoilsList());
        List<Node> restrictedNode=new ArrayList<>();
        restrictedNode.addAll(mapInfo.getVirussList());
        restrictedNode.addAll(mapInfo.getBalks());
        restrictedNode.addAll(mapInfo.getBombList());
        restrictedNode.addAll(mapInfo.getDHumanList());
        restrictedNode.addAll(mapInfo.getQuarantines());
        restrictedNode.addAll(mapInfo.getTeleportGates());
        restrictedNode.addAll(mapInfo.getWalls());
        List<Node> blank=mapInfo.getBlanks();
        blank.removeAll(safeNode);
        blank.removeAll(restrictedNode);
        safeNode.addAll(blank);
        safeNode.removeIf(Objects::isNull);
        System.out.println("Int array: ");
        for(Node i:safeNode)
            System.out.println(i);
        if (safeNode.isEmpty())
            return Dir.DROP_BOMB;
        try{
            Collections.sort(safeNode, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    if (o1.getValue()==o2.getValue())
                    {
                        return BaseAlgorithm.manhattanDistance(o1,mapInfo.getCurrentPosition(hero))-BaseAlgorithm.manhattanDistance(o2,mapInfo.getCurrentPosition(hero));
                    }
                    return o2.getValue()-o1.getValue();
                }
            });
        }catch(Exception e){

        }



        Node startNode=Node.createFromPosition(mapInfo.getCurrentPosition(hero));
        for (int i=0;i<safeNode.size();i++)
        {
            Stack<Node> steps =  AStarSearch.aStarSearch(map, restrictedNode, startNode, safeNode.get(i));
            if (!steps.empty())
            {
                System.out.println("go to node"+safeNode.get(i));
                if (blank.contains(safeNode.get(i)))
                {
                    //datbom
                    Map<Node, Stack<Node>> pathToAllBoxs = sortByComparator(AStarSearch.getPathsToAllFoods(mapInfo, hero, mapInfo.getBalks(), false), false);
                    for (Map.Entry<Node, Stack<Node>> path : pathToAllBoxs.entrySet()) {
                        List<Node> listPlaceBom = getPlacingBom(path.getKey(), false,mapInfo,hero);
                        String step = getSimpleBombPath( listPlaceBom, mapInfo,hero);
                        if (!step.isEmpty()) {
                            return step;
                        }
                    }
                }
                else
                {
                    String path=BaseAlgorithm.getStepsInString(startNode, steps);
                    System.out.println(path+"path");
                    try{
                        Node next=Node.createFromPosition(mapInfo.getCurrentPosition(hero)).nextPosition(Integer.parseInt(path),1);
                        System.out.println(next+"next");
                        if (next!=null)
                        {
                            if (mapInfo.getBalks().contains(next))
                                return Dir.DROP_BOMB;

                        }

                    }catch(Exception e){

                    }
                    return path;
                }

            }

        }
return Dir.INVALID;
//        for (Node i:safeNode)
//        {
//            System.out.println(i.toString()+" value:" +i.getValue());
//        }
//
//        return Dir.INVALID;
    }

    public static void main(String[] aDrgs) {
        Hero player1 = new Hero("player1-xxx", GameConfig.GAME_ID);
        Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo = gameInfo.map_info;

            Position currentPosition = mapInfo.getCurrentPosition(player1);
            Position enemyPosisiton = mapInfo.getEnemyPosition(player1);
            List<Position> restrictPosition =  new ArrayList<Position>();

            player1.move(tactic(mapInfo,player1));
        };
        player1.setOnTickTackListener(onTickTackListener);
        player1.connectToServer();
    }
}
