package exercise1;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.dbms.*;
import de.hpi.dbs2.dbms.utils.BlockSorter;
import de.hpi.dbs2.exercise1.SortOperation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

@ChosenImplementation(true)
public class TPMMSJava extends SortOperation {
    public TPMMSJava(@NotNull BlockManager manager, int sortColumnIndex) {
        super(manager, sortColumnIndex);
    }

    @Override
    public int estimatedIOCost(@NotNull Relation relation) {
        // TODO implement
        // TODO return 3*"Blockzahl";
        throw new UnsupportedOperationException("TODO");
    }


    private ArrayList<ArrayList<Block>> firstPhaseSort(@NotNull Relation relation) {

        ArrayList<Block> loadedBlocks = new ArrayList<>();
        ArrayList<ArrayList<Block>> allBlocks = new ArrayList<>();
        BlockSorter blockSorter = BlockSorter.INSTANCE;
        for (Block block : relation) {
            getBlockManager().load(block);
            loadedBlocks.add(block);
            if (getBlockManager().getFreeBlocks() == 0) {
                sortLoadedBlocks(relation, loadedBlocks, blockSorter, allBlocks);
                loadedBlocks = new ArrayList<>();
            }
        }
        if (!loadedBlocks.isEmpty())
            sortLoadedBlocks(relation, loadedBlocks, blockSorter, allBlocks);

        return allBlocks;
    }

    private void sortLoadedBlocks(@NotNull Relation relation, ArrayList<Block> loadedBlocks, BlockSorter blockSorter, ArrayList<ArrayList<Block>> allBlocks) {

        blockSorter.sort(relation, loadedBlocks, relation.getColumns().getColumnComparator(getSortColumnIndex()));
        for (Block listBlock : loadedBlocks) {
            getBlockManager().release(listBlock, true);
        }
        allBlocks.add(loadedBlocks);
    }


    private void secondPhaseSort(@NotNull Relation relation, BlockOutput blockOutput, ArrayList<ArrayList<Block>> allBlockLists) {
        // TODO laden immer so viele Blöcke, dass wir noch einen Platz haben, also bei 3 capacity, müssten wir nur 2 Laden dürfen
        ArrayList<LinkedList<Tuple>> loadedTuples = new ArrayList<>();
        for (ArrayList<Block> blockList : allBlockLists) {
            Block block = blockList.get(0);
            getBlockManager().load(block);
            loadedTuples.add(new LinkedList<>());
            for (Tuple tuple : block) {
                loadedTuples.get(loadedTuples.size() - 1).add(tuple);
            }
        }
        Block outBlock = getBlockManager().allocate(true);
        while (true) {
            int smallestIndex = -1; // not always 0 to begin with
            for (int i = 0; i < loadedTuples.size(); i++) {
                // HERE
                if (allBlockLists.get(i).size() == 0) {
                    continue;
                }
                if (loadedTuples.get(i).size() == 0) {
                    getBlockManager().release(allBlockLists.get(i).get(0), false);
                    allBlockLists.get(i).remove(0);
                    if (allBlockLists.get(i).size() == 0) {
                        continue;
                    } else {
                        Block recentlyLoadedBlock = allBlockLists.get(i).get(0);
                        getBlockManager().load(recentlyLoadedBlock);
                        for (Tuple tuple : recentlyLoadedBlock) {
                            loadedTuples.get(i).add(tuple);
                        }
                    }
                }

                if (smallestIndex == -1) {
                    smallestIndex = i;
                    continue;
                }

                Tuple smallestTuple = loadedTuples.get(i).get(0);
                Tuple beforeSmallestTuple = loadedTuples.get(smallestIndex).get(0);
                if (relation.getColumns().getColumnComparator(getSortColumnIndex()).compare(beforeSmallestTuple, smallestTuple) > 0) {
                    smallestIndex = i;
                }

            }
            if (smallestIndex == -1)
                break;

            if (outBlock.isFull()) {
                blockOutput.output(outBlock);
                getBlockManager().release(outBlock, false);
                outBlock = getBlockManager().allocate(true);
            }
            outBlock.append(loadedTuples.get(smallestIndex).get(0));
            loadedTuples.get(smallestIndex).removeFirst();
        }
        blockOutput.output(outBlock);
        getBlockManager().release(outBlock, false);
    }

    @Override
    public void sort(@NotNull Relation relation, @NotNull BlockOutput output) {
        // TODO test memory size, can fit all blocks??


        ArrayList<ArrayList<Block>> allBlocks = firstPhaseSort(relation);
        secondPhaseSort(relation, output, allBlocks);
    }
}
