package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node
{
    Node parent;
    List<Node> children;
    PentagoBoardState board;
    int nbVisits;
    int nbWins;
    PentagoMove move;

    //constructor
    public Node(PentagoBoardState current)
    {
        this.board = current;
        this.parent = null;
        this.children = new ArrayList<Node>();
        this.nbVisits = 0;
        this.nbWins = 0;
        this.move = null;
    }

    //constructor
    public Node(PentagoBoardState current, PentagoMove move, Node parent)
    {
        this.board = current;
        this.parent = parent;
        this.children = new ArrayList<Node>();
        this.nbVisits = 0;
        this.nbWins = 0;
        this.move = move;
    }

    public void setParent(Node parent)
    {
        this.parent = parent;
    }

    //used as the expansion step
    public void setChildren()
    {
        //for all legal move create a node and add to children
        for(PentagoMove current : this.board.getAllLegalMoves())
        {
            PentagoBoardState temp = (PentagoBoardState) this.board.clone();
            temp.processMove(current);

            //make sure there are no duplicate child nodes
            if(!sameMove(temp))
            {
                this.children.add(new Node(temp,current,this));
            }


        }
        //shuffle child array to ensure we do not alway pick nodes from the same quadrant
        Collections.shuffle(this.children);
    }

    //check if 2 moves are the same
    public boolean sameMove(PentagoBoardState temp)
    {
        for(Node child : this.children)
        {
            String a = child.board.toString();
            String b = temp.toString();

            if(a.equals(b))
            {
                return true;
            }
        }
        return false;
    }

    //check if 2 nodes are the same
    public boolean isEqual(Node aNode)
    {
        String a = aNode.board.toString();
        String b = this.board.toString();

        if(a.equals(b))
        {
            return true;
        }

        return false;
    }
}

