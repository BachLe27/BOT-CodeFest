package jsclub.codefest.sdk.socket.data;

import com.google.gson.Gson;

import jsclub.codefest.sdk.constant.MapEncode;
import jsclub.codefest.sdk.model.Hero;

import java.util.ArrayList;
import java.util.List;

public class MapInfo {
    public String myId;
    public MapSize size;
    public List<Player> players;
    public List<int[]> map; // lay ra nhung o co dinh
    public List<Bomb> bombs;
    public List<Spoil> spoils;
    public List<Gift> gifts;
    public List<Viruses> viruses;
    public List<Human> human;
    public List<Position> walls = new ArrayList<>();
    public List<Position> balk = new ArrayList<>();
    public List<Position> blank = new ArrayList<>();
    public List<Position> teleportGate = new ArrayList();
    public List<Position> quarantinePlace = new ArrayList();

    public Player getPlayerByKey(String key) {
        Player player = null;
        if (players != null) {
            for (Player p : players) {
                if (key.startsWith(p.id)) {
                    player = p;
                    break;
                }
            }
        }
        return player;
    }

    public List<Viruses> getVirus() {
        return viruses;
    }

    public List<Human> getDhuman() {
        List<Human> dhumanList = new ArrayList<>();
        if (human != null) {
            for (Human dhuman : human) {
                if (dhuman.infected) {
                    dhumanList.add(dhuman);
                }
            }
        }
        return dhumanList;
    }

    public List<Human> getNHuman() {
        List<Human> nhumanList = new ArrayList<>();
        if (human != null) {
            for (Human nhuman : human) {
                if (!nhuman.infected && nhuman.curedRemainTime == 0) {
                    nhumanList.add(nhuman);
                }
            }
        }
        return nhumanList;
    }

    public void updateMapInfo() {
        for (int i = 0; i < size.rows; i++) {
            int[] map = this.map.get(i);
            for (int j = 0; j < size.cols; j++) {
                switch (map[j]) {
                    case MapEncode.ROAD:
                        blank.add(new Position(j, i));
                        break;
                    case MapEncode.WALL:
                        walls.add(new Position(j, i));
                        break;
                    case MapEncode.BALK:
                        balk.add(new Position(j, i));
                        break;
                    case MapEncode.TELEPORT_GATE:
                        teleportGate.add(new Position(j, i));
                        break;
                    case MapEncode.QUARANTINE_PLACE:
                        quarantinePlace.add(new Position(j, i));
                        break;
                    default:
                        walls.add(new Position(j, i));
                        break;
                }
            }
        }

        for (int i = 0; i < size.rows; i++) {
            int[] map = this.map.get(i);
            for (int j = 0; j < size.cols; j++) {
                System.out.print(map[j] + " ");
            }
            System.out.println();
        }

    }



    // Todo: Convert all data in MapInfo to List<Node>
    // EG: List<Viruses> viruses -> List<Node> viruses with value = 3
    // Bomb = 2
    // DHuman = 3
    // NHuman = 5
    // Spoil = 7
    // Viruses = 3
    // BALK = 6
    // Wall = 0
    // TELEPORT_GATE = 1
    // QUARANTINE_PLACE = -1
    // blank = 4

    public List<Node> getBalks() {
        int balkValue = 6;
        List<Node> balks = new ArrayList<>();
        for (Position balk: this.balk) {
            Node temp = Node.createFromPosition(balk);
            temp.setValue(balkValue);
            balks.add(temp);
        }
        return balks;
    }

    public List<Node> getWalls() {
        int wallValue = 0;
        List<Node> walls = new ArrayList<>();
        for (Position wall: this.walls) {
            Node temp = Node.createFromPosition(wall);
            temp.setValue(wallValue);
            walls.add(temp);
        }
        return walls;
    }

    public List<Node> getTeleportGates() {
        int teleportGateValue = 0;
        List<Node> teleportGates = new ArrayList<>();
        for (Position gate: this.teleportGate) {
            Node temp = Node.createFromPosition(gate);
            temp.setValue(teleportGateValue);
            teleportGates.add(temp);
        }
        return teleportGates;
    }

    public List<Node> getQuarantines() {
        int quarantineValue = -1;
        List<Node> quarantines = new ArrayList<>();
        for (Position quarantine: this.quarantinePlace) {
            Node temp = Node.createFromPosition(quarantine);
            temp.setValue(quarantineValue);
            quarantines.add(temp);
        }
        return quarantines;
    }

    public List<Node> getBlanks() {
        int blankValue = 4;
        List<Node> blanks = new ArrayList<>();
        for (Position blank: this.blank) {
            Node temp = Node.createFromPosition(blank);
            temp.setValue(blankValue);
            blanks.add(temp);
        }
        return blanks;
    }

    //lay ra list Node cac viruss va node viruss sap di toi
    public List<Node> getDHumanList() {
        List<Node> list = new ArrayList<>();
        for (Human i : getDhuman()) {
            Node viruss = Node.createFromPosition(i.position);
            viruss.setValue(3);
            list.add(viruss);
            list.add(viruss.nextPosition(i.direction,1));
        }
        //blank.removeall(list);
        return list;
    }
    public List<Node> getNHumanList() {
        List<Node> list = new ArrayList<>();
        for (Human i : getNHuman()) {
            Node viruss = Node.createFromPosition(i.position);
            viruss.setValue(5);
            list.add(viruss);
            list.add(viruss.nextPosition(i.direction,1));
        }
        //blank.removeall(list);
        return list;
    }
    public List<Node> getVirussList() {
        List<Node> list = new ArrayList<>();
        for (Viruses i : this.viruses) {
            Node viruss = Node.createFromPosition(i.position);
            viruss.setValue(3);
            list.add(viruss);
            Node next=viruss.nextPosition(i.direction,1);
            if (next!=null)
            {
                next.setValue(3);

                list.add(next);
            }
            next=viruss.nextPosition(i.direction,2);
            if (next!=null)
            {
                next.setValue(3);

                list.add(next);
            }
        }
        //blank.removeall(list);
        return list;
    }
    public List<Node> getSpoilsList() {
        List<Node> list = new ArrayList<>();
        for (Spoil i : this.spoils) {
            Node spoil = new Node(i.col,i.row);
            spoil.setValue(7);
            list.add(spoil);
        }
        //blank.removeall(list);
        return list;
    }
    //list vi tri cua bomb va vi tri sap no
    public List<Node> getBombList(){
        List<Node> list=new ArrayList<>();
        for (Bomb i: this.bombs)
        {
            Node bomb=new Node(i.col,i.row);
            bomb.setValue(2);
            list.add(bomb);
            Player player=getPlayerByKey(i.playerId);
            for (int d=1;d<5;d++)
            {
                for (int p=1;p<=player.power;p++)
                {
                    Node effBomb=bomb.nextPosition(d,p);
                    effBomb.setValue(2);
                    list.add(effBomb);
                    if (this.walls.contains(effBomb)|| (this.balk.contains(effBomb)))
                    {
                        if (this.balk.contains(effBomb));
                            //remove box o node nay
                        break;
                    }
                }
            }
        }
        return list;
    }
    public int[][] getMap() {

        int[][] newMap = new int[size.rows][size.cols];

        for (int i = 0; i < size.rows; i++) {
            int[] map = this.map.get(i);
            for (int j = 0; j < size.cols; j++) {
                newMap[i][j] = map[j];
            }
        }

        return newMap;
    }

    public Position getEnemyPosition(Hero hero) {
        Position position = null;
        if (hero != null) {
            for (Player player : players) {
                if (!hero.getPlayerName().startsWith(player.id)) {
                    position = player.currentPosition;
                    break;
                }
            }
        }
        return position;
    }

    public Position getCurrentPosition(Hero hero) {
        Position position = null;
        if (hero != null) {
            for (Player player : players) {
                if (hero.getPlayerName().startsWith(player.id)) {
                    position = player.currentPosition;
                    break;
                }
            }
        }
        return position;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
