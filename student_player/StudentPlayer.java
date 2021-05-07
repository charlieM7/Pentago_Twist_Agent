package student_player;

import boardgame.Move;

import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer
{
    public Tree tree;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer()
    {
        super("260929736");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState)
    {
        //set the start time of the method
        long startTime = System.currentTimeMillis();
        int player = boardState.getTurnPlayer();
        int opponent = 1 - player;
        Node root = new Node(boardState);

        MonteCarloTreeSearch MCTS = new MonteCarloTreeSearch(root,player);

        //set the end time of the method
        long end = 1900;

        //if on the first iteration
        if(boardState.getTurnNumber() == 0)
        {
            //increate the end time and create tree
            end = 25000;
            this.tree = new Tree(root);
        }

        else
        {
            //else find the child that is represented by boardstate in the tree
            for(Node current : tree.root.children)
            {
                if(current.isEqual(root))
                {
                    root = current;
                    root.setParent(null);
                }
            }
        }

        //run until timer is up
        while(System.currentTimeMillis() - startTime < end)
        {
            //selection step
            Node selected = MCTS.selection(root);

            //if we are not at a winning state
            if(!selected.board.gameOver())
            {
                //expansion step
                selected.setChildren();
            }

            //if the selected node has children
            if(selected.children.size() > 0)
            {
                //select one at random
                int rand = (int) (Math.random() * selected.children.size());
                Node child = selected.children.get(rand);
                selected = child;
            }

            //simulation and backpropagation steps
            int win = MCTS.simulation(selected);
            MCTS.backProp(win, selected, root);
        }

        //get best child
        Node best = MCTS.bestChild(root);

        //if it returns an illegal move, select a random move
        if(!root.board.isLegal(best.move))
        {
            return root.board.getRandomMove();
        }

        //set new root
        tree.setRoot(best);

        return best.move;
    }
}