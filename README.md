# Pentago_Twist_Agent

# Description

Pentago Twist is a two player game played on a board that is split into quadrants each can be flipped or rotated by 90 degrees at each turn. The goal of the game is to place five of your chips in a row. On each turn a player places their chip on the board and then chooses to either flip or rotate any quadrant. In this final project, we were tasked with creating an agent capable of playing pentago twist. The Base code was from [Samin Yeasar](https://github.com/SaminYeasar/pentago_twist)

# Set Up
1. Download [the base code repo](https://github.com/SaminYeasar/pentago_twist) created by Samin Yeasar
2. Replace pentago_twist/scr/student_player by the code provided here

# Code Explanation
To solve this project, we opted for a Monte Carlo Tree Search algorithm. The project had a time constraint as well as a memory constraint, further, Pentago Twist has a high complexity which justified the choice of use of Monte Carlo Tree Search.

When creating an agent that will play a given game it is important to consider the games complexity as well as any other constraints that may be specified. In the case of Pentago Twist, the complexity is higher and we have time and memory constraints which will affect our decision of which algorithm to use. Minmax and alpha-beta pruning algorithms are expensive and both require the use of an evaluation function which can lead to less than optimal game play if not selected properly, I thus opted for the use of Monte Carlo Tree Search (MCTS) algorithm. MCTS relies on the use of random simulations to help select a move that will lead to a favourable outcome. The use of random sim- ulations allows for less memory to be allocated to find a solution. In fact MCTS can be stopped at anytime and will always return the state that is the most favourable so far. Furthermore, MCTS uses reinforcement learning in order to assign values to states

## StudentPlayer.py

### chooseMove()
- When the method is called a timer is started that keep track of the time elapsed since it was called
- At the start a tree is initialized that that keeps track of visited board states 
- A for-loop finds the board state that has been passed as argument to the ”chooseMove” method, in the MCTS tree that was initialized
on the first turn
- Once the child is found it is set as the root of the tree and we can enter the while loop
- Within this while loop, we go through the four MCTS steps mentioned above, starting with selection

```
 public Move chooseMove(PentagoBoardState boardState)
 ```
 
 ## Node.py
 
 ### setChildren()
 - method used to expand a node by getting all possible moves for the node state and adds them as children making sure there are no duplicate
states

```
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
```

### sameMove()
- used to check if two moves are the same

```
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
```

### isEqual()
- Check if two nodes are the same

```
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
```

## MonteCarloTreeSearch

### selection()
- Get node for expansion
- Uses the upper confidence tree (UCT) as a tree policy to find a good node to later be expanded.

```
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
```

### simulation()
- Once expansion is complete, simulation will simulate a rollout from the selected node using the default policy which is random
- Once the simulation reaches a winning state it will return -1 if it reaches a loss, 0 if it reaches a draw and 1 for a win

```
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
```

### UCT()
- Used to calculate UCT value for a node

```
 //implementation of UCT from class notes
    public static double UCT(Node child)
    {
        Node parent = child.parent;
        double fraction = child.nbWins/ child.nbVisits;
        double fraction2 = Math.log(parent.nbVisits)/ child.nbVisits;
        double value = fraction + Math.sqrt(2 * fraction2);

        return value;
    }
```

### backProp()
- After the simulation has been completed the results obtained must be back-propagated up the tree

```
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
```

### bestChild()
- When the code breaks out of the while loop regulated by the timer the ”bestChild()” method is called and used the nbWins to return the state that is most likely to result in a win for the agent
- the algorithm will look at all possible moves for a given child and set a min value to a child if a state is found that can return a win for the opponent

```
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
```
