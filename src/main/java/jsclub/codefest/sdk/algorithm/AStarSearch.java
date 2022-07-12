package jsclub.codefest.sdk.algorithm;

import jsclub.codefest.sdk.constant.MapEncode;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Node;
import jsclub.codefest.sdk.socket.data.Position;
import java.util.*;

public class AStarSearch extends BaseAlgorithm{
    public static  String aStarSearch(int[][] matrix, List<Position> restrictNode, Position start, Position end) {
        Node startNode = Node.createFromPosition(start);
        Node endNode = Node.createFromPosition(end);

        List<Node> restrictNodeList = new ArrayList<Node>();
        for (Position position : restrictNode) {
            restrictNodeList.add(Node.createFromPosition(position));
        }

        Stack<Node> steps =  aStarSearch(matrix, restrictNodeList, startNode, endNode);
        return getStepsInString(startNode, steps);
    }

    public static Stack<Node> aStarSearch(int[][] matrix, List<Node> restrictNode, Node start, Node target) {
        int mMapWidth = matrix.length;
        int mMapHeight = matrix[0].length;

        ArrayList<Node> openList = new ArrayList<>();
        ArrayList<Node> closeList = new ArrayList<>();
        Stack<Node> stack = new Stack<>();// Elephant to eat the path
        openList.add(Node.createFromPosition(start));// Place the start Node in the open list;
        start.setH(manhattanDistance(start, target));

        while (!openList.isEmpty()) {
            Node now = null;
            int minValue = Integer.MAX_VALUE;
            for (Node n : openList) {// We find the F value (the description farthest from the target), if the same
                // we choose behind the list is the latest addition.
                if (n.getF() < minValue) {
                    minValue = n.getF();
                    now = n;
                }
                if (now != null && n.getF() == minValue
                        && (distanceBetweenTwoPoints(n, start) < distanceBetweenTwoPoints(now, start))) {
                    now = n;
                }

            }
            // Remove the current Node from the open list and add it to the closed list
            openList.remove(now);
            closeList.add(now);
            // Neighbor in four directions
            Node left = Node.createFromPosition(now.leftPosition(1));
            Node right = Node.createFromPosition(now.rightPosition(1));
            Node up = Node.createFromPosition(now.upPosition(1));
            Node down = Node.createFromPosition(now.downPosition(1));
            List<Node> temp = new ArrayList<>(4);
            temp.add(up);
            temp.add(right);
            temp.add(down);
            temp.add(left);
            for (Node n : temp) {
                // If the neighboring Node is not accessible or the neighboring Node is already
                // in the closed list, then no action is taken and the next Node continues to be
                // examined;
                if (
                    (!n.equals(target) && ! isValidNode(matrix, n, restrictNode))
                    || closeList.contains(n)
                    || n.getX() > mMapWidth
                    || n.getX() < 1
                    || n.getY() > mMapHeight
                    || n.getY() < 1) {
                    continue;
                }

                // If the neighbor is not in the open list, add the Node to the open list,
                // and the adjacent Node'elephant father Node as the current Node, while saving the
                // adjacent Node G and H value, F value calculation I wrote directly in the Node
                // class
                if (!openList.contains(n)) {
                    // Logger.println("ok");
                    n.setFather(now);
                    n.setG(now.getG() + 1);
                    n.setH(manhattanDistance(n, target));
                    openList.add(n);
                    // When the destination Node is added to the open list as the Node to be
                    // checked, the path is found, and the loop is terminated and the direction is
                    // returned.
                    if (n.equals(target)) {
                        // Go forward from the target Node, .... lying groove there is a pit, Node can
                        // not use f, because f and find the same Node coordinates but f did not record
                        // father
                        Node node = openList.get(openList.size() - 1);
                        while (node != null
//                                && !node.equals(playerNode)???????
                        ) {
                            stack.push(node);
                            node = node.getFather();
                        }
                        // Create previous step to finding out next step

                        return stack;
                    }
                }
                // If the neighbor is in the open list,
                // // judge whether the value of G that reaches the neighboring Node via the
                // current Node is greater than or less than the value of G that is stored
                // earlier than the current Node (if the value of G is greater than or smaller
                // than the value of G), set the father Node of the adjacent Node as Current
                // Node, and reset the G and F values ​​of the adjacent Node.
                if (openList.contains(n)) {
                    if (n.getG() > (now.getG() + 1)) {
                        n.setFather(now);
                        n.setG(now.getG() + 1);
                    }
                }
            }
        }
        // When the open list is empty, indicating that there is no new Node to add, and
        // there is no end Node in the tested Node, the path can not be found. At this
        // moment, the loop returns -1 too.
        return new Stack<>();
    }

    static Boolean isValidNode(int[][] matrix, Node n, List<Node> restrictNode) {
        if (matrix[n.getY()][n.getX()] == MapEncode.ROAD) {
            return true;
        }

        if (matrix[n.getY()][n.getX()] == MapEncode.WALL || matrix[n.getY()][n.getX()] == MapEncode.BALK
        || matrix[n.getY()][n.getX()] == MapEncode.QUARANTINE_PLACE || matrix[n.getY()][n.getX()] == MapEncode.TELEPORT_GATE) {
            return false;
        }

        return !restrictNode.contains(n);
    }
    public static Map<Node, Stack<Node>> getPathsToAllFoods(MapInfo mapInfo, Hero hero, List<Node> targets, boolean isCollectSpoils) {
        List<Node> restrictedNode=new ArrayList<>();
        restrictedNode.addAll(mapInfo.getVirussList());
        restrictedNode.addAll(mapInfo.getBalks());
        restrictedNode.addAll(mapInfo.getBombList());
        restrictedNode.addAll(mapInfo.getDHumanList());
        restrictedNode.addAll(mapInfo.getQuarantines());
        restrictedNode.addAll(mapInfo.getTeleportGates());
        restrictedNode.addAll(mapInfo.getWalls());
        Map<Node, Stack<Node>> allPaths = new HashMap<>();
        Queue<Node> open = new LinkedList<>();
        Set<String> visited = new HashSet<>();// Record the visited Node
        List<Node> target = new ArrayList<>(targets);
        open.add(Node.createFromPosition(mapInfo.getCurrentPosition(hero)));
        //open.add(clonePlayer.getPosition());
        while(!open.isEmpty()) {
            Node now = open.remove();
            if (target.isEmpty()) {
                return allPaths;
            }
            for (Node food : target) {
                if (food.getX() == now.getX() && food.getY() == now.getY()) {
                    Stack<Node> paths = new Stack<>();
                    Node node = now;
                    while (node != null && !node.equals(Node.createFromPosition(mapInfo.getCurrentPosition(hero)))) {
                        paths.push(node);
                        node = node.getFather();
                    }
                    allPaths.put(food, paths);
                    target.remove(food);
                    break;
                }
            }
            Node left = new Node(now.getX() - 1, now.getY());
            Node right = new Node(now.getX() + 1, now.getY());
            Node up = new Node(now.getX(), now.getY() - 1);
            Node down = new Node(now.getX(), now.getY() + 1);
            if (!restrictedNode.contains(up) && !visited.contains(up.toString()) && up.getX() <= mapInfo.size.cols
                    && up.getX() >= 1 && up.getY() <= mapInfo.size.rows  && up.getY() >= 1 && (!isCollectSpoils || !mapInfo.getBalks().contains(up))) {
                up.setFather(now);
                open.add(up);
                visited.add(up.toString());
            }
            if (!restrictedNode.contains(right) && !visited.contains(right.toString())
                    && right.getX() <= mapInfo.size.cols && right.getX() >= 1 && right.getY() <= mapInfo.size.rows
                    && right.getY() >= 1 && (!isCollectSpoils || !mapInfo.getBalks().contains(right))) {
                right.setFather(now);
                open.add(right);
                visited.add(right.toString());
            }
            if (!restrictedNode.contains(down) && !visited.contains(down.toString()) && down.getX() <= mapInfo.size.cols
                    && down.getX() >= 1 && down.getY() <= mapInfo.size.rows && down.getY() >= 1
                    && (!isCollectSpoils || !mapInfo.getBalks().contains(down))) {
                down.setFather(now);
                open.add(down);
                visited.add(down.toString());
            }
            if (!restrictedNode.contains(left) && !visited.contains(left.toString()) && left.getX() <= mapInfo.size.cols
                    && left.getX() >= 1 && left.getY() <= mapInfo.size.rows && left.getY() >= 1
                    && (!isCollectSpoils || !mapInfo.getBalks().contains(left))) {
                left.setFather(now);
                open.add(left);
                visited.add(left.toString());
            }
        }
        return allPaths;
    }
}
