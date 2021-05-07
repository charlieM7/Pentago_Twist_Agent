package student_player;

import boardgame.Board;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.ArrayList;

public class MonteCarloTreeSearch
{
    public static int player;
    public static int opponent;
    public Node root;


    public MonteCarloTreeSearch(Node root, int colour)
    {
        this.root = root;
        this.player = colour;
        this.opponent = 1 - colour;
    }

    public static double getSomething() {
        return Math.random();
    }

    //simualtion step from MCTS
    public static int simulation(Node selected)
    {
        PentagoBoardState current = (PentagoBoardState) selected.board.clone();

        //if current state gets win for opponent
        if(current.getWinner() == opponent)
        {
            if(selected.parent != null)
            {
                //set wins to min value
                selected.parent.nbWins = Integer.MIN_VALUE;
                return current.getWinner();
            }
        }

        //if current state get win for player
        if(current.getWinner() == player)
        {
            if(selected.parent != null)
            {
                //set wins to max value
                selected.parent.nbWins = Integer.MAX_VALUE;
                return current.getWinner();
            }
        }

        //while not at a winning board state
        while(current.getWinner() == Board.NOBODY)
        {
            //get all moves and select one at random
            ArrayList<PentagoMove> moves = current.getAllLegalMoves();
            int rand = (int) (getSomething() * moves.size());
            PentagoMove next = moves.get(rand);
            //process move to get new board state
            current.processMove(next);
        }

        return current.getWinner();
    }

    //get node for expansion
    public static Node selection(Node root)
    {
        Node current = root;

        //while the node has children, go until reach a leaf node
        while(current.children.size() > 0)
        {
            Node best = current;
            double max = Integer.MIN_VALUE;

            //get best child
            for(Node child : current.children)
            {
                double value;

                //if child has not been visited set value to max value
                if(child.nbVisits == 0)
                {
                    value = Integer.MAX_VALUE;
                }

                else
                {
                    value = UCT(child);
                }

                //pick highest value
                if(value > max)
                {
                    max = value;
                    best = child;
                }
            }
            current = best;
        }
        return current;
    }

    //implementation of UCT from class notes
    public static double UCT(Node child)
    {
        Node parent = child.parent;
        double fraction = child.nbWins/ child.nbVisits;
        double fraction2 = Math.log(parent.nbVisits)/ child.nbVisits;
        double value = fraction + Math.sqrt(2 * fraction2);

        return value;
    }

    //backpropagate results from simulation back up the tree
    public void backProp(int win, Node selected, Node root)
    {
        Node current = selected;

        //while we have not reached the root update visits and wins
        while(current.parent != null)
        {
            current.nbVisits++;

            if(current.board.getTurnPlayer() == win)
            {
                current.nbWins += 1;
            }

            else
            {
                current.nbWins -= 1;
            }

            current = current.parent;
        }
        //update visits and wins for root
        current.nbVisits++;

        if(current.board.getTurnPlayer() == win)
        {
            current.nbWins += 1;
        }

        else
        {
            current.nbWins -= 1;
        }
    }

    //retrieve best child to return
    public Node bestChild(Node root)
    {
        double maxValue = Integer.MIN_VALUE;
        Node bestChild = root;

        //get state that has the higher win number
        for(Node child : root.children)
        {
            double value;


            if(child.nbVisits == 0)
            {
                value = 0;
            }

            else
            {
                value = child.nbWins;

                if(child.board.getWinner() == player)
                {
                    value = Integer.MAX_VALUE;
                    return child;
                }

                //if a node has a child that will return a loss set value to min
                for(PentagoMove aMove : child.board.getAllLegalMoves())
                {
                    PentagoBoardState aState = (PentagoBoardState) child.board.clone();
                    aState.processMove(aMove);
                    if(aState.getWinner() != Board.NOBODY && aState.getWinner() != player)
                    {
                        value = Integer.MIN_VALUE;
                    }

                }


            }

            //pick node with higher value
            if(value > maxValue)
            {
                maxValue = value;
                bestChild = child;
            }
        }
        return bestChild;
    }
}
