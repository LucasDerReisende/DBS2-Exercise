package exercise2;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.exercise2.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * This is the B+-Tree implementation you will work on.
 * Your task is to implement the insert-operation.
 */
@ChosenImplementation(false)
public class BPlusTreeJava extends AbstractBPlusTree {
    public BPlusTreeJava(int order) {
        super(order);
    }

    public BPlusTreeJava(BPlusTreeNode<?> rootNode) {
        super(rootNode);
    }
/*
TODO uncomment
    private void insertIntoNodeWithoutOverflow(BPlusTreeNode<?> node, Integer key, ValueReference value) {
        // TODO
        Integer intermediateKey = null;
        BPlusTreeNode intermediateValue = null;
        for (int i = 0; i < node.keys.length; i++) {
            if (node.keys[i] > key && intermediateKey == null) {
                intermediateKey = node.keys[i];
                intermediateValue = (ValueReference) node.references[i];

            }
        }
    }
*/

    private void insertIntoNodeWithoutOverflow(InnerNode node, Integer key, BPlusTreeNode<?> value) {
        Integer intermediateKey;
        BPlusTreeNode<?> intermediateValue;
        int shift = 0;
        for (int i = 0; i < node.keys.length; i++) {
            intermediateKey = node.keys[i];
            intermediateValue = node.references[i];
            if (node.keys[i] > key && shift == 0) {
                node.keys[i] = key;
                node.references[i] = value;
                shift = 1;
            }
            node.keys[i + shift] = intermediateKey;
            node.references[i + shift] = intermediateValue;
        }
    }

    private void insertIntoNodeWithoutOverflow(LeafNode node, Integer key, ValueReference value) {
        Integer intermediateKey;
        ValueReference intermediateValue;
        int shift = 0;
        for (int i = 0; i < node.keys.length; i++) {
            intermediateKey = node.keys[i];
            intermediateValue = node.references[i];
            if (node.keys[i] > key && shift == 0) {
                node.keys[i] = key;
                node.references[i] = value;
                shift = 1;
            }
            node.keys[i + shift] = intermediateKey;
            node.references[i + shift] = intermediateValue;
        }
    }

    private ValueReference insertIntoNodeWithOverflow(BPlusTreeNode<?> node, Integer key, ValueReference value) {
        // TODO
        /* oh shit, overflow*/
        /* in methode */
        // neuen node erstellen, rechts neben bereits existierendem
        // keys und values auf diese verteilen
        // "next leaf"-pointers updaten

        if (/* checke ob n eine Wurzel war, wenn ja*/) {
            // erstelle neue Wurzel
            // mache die beiden als kinder
            return // fertig return
        }

        if (/*blatt*/) {
            /* kopiere kleinster aus n2 nach oben*/
        } else {
            /* ziehe größten Schlüssel in n1 nach oben*/
        }
        /* insert gemäß obriger Methode*/

        /* bei oh shit overflow weitermachen mit elternknoten */
        return null;
    }

    private ValueReference insertIntoLeafNodeWithOverflow(LeafNode node, Integer key, ValueReference value) {
        // TODO
        /* oh shit, overflow*/
        /* in methode */
        // neuen node erstellen, rechts neben bereits existierendem
        // keys und values auf diese verteilen
        // "next leaf"-pointers updaten

        boolean isRootNode = this.rootNode.equals(node);

        int beforeCount = (int) node.getEntries().count();
        // firstCount is the number of entries in the left node after split
        // +2 -> +1 for additional element, +1 for correct division
        int firstCount = (beforeCount + 2) / 2;

        Entry[] entries = new Entry[beforeCount + 1 - firstCount];
        for (int i = firstCount; i < beforeCount; i++) {
            entries[i-firstCount] = new Entry(node.keys[i], node.references[i]);
            node.keys[i] = null;
            node.references[i] = null;
        }
        LeafNode nextNode = new LeafNode(node.order, entries);
        nextNode.nextSibling = node.nextSibling;
        node.nextSibling = nextNode;

        if (isRootNode) {
            // erstelle neue Wurzel
            // setze node und nextNode als Kinder
            BPlusTreeNode<?>[] nodes = {node, nextNode};
            this.rootNode = new InnerNode(node.order, nodes);
            return value;
        }

        if (/*blatt*/) {
            /* kopiere kleinster aus n2 nach oben*/
        } else {
            /* ziehe größten Schlüssel in n1 nach oben*/
        }
        /* insert gemäß obriger Methode*/

        /* bei oh shit overflow weitermachen mit elternknoten */
        return null;
    }

    private void splitNode(BPlusTreeNode<?> node) {

        return;
    }


    private void updateParents(Integer key) {
        Stack<InnerNode> pathStack = getPathStack(key);
        while (!pathStack.empty()) {
            InnerNode topNode = pathStack.pop();
            for (int i = 0; i < topNode.keys.length; i++) {
                if (topNode.keys[i] == null)
                    break;
                if (!Objects.equals(topNode.keys[i], topNode.references[i + 1].getSmallestKey())) {
                    topNode.keys[i] = topNode.references[i + 1].getSmallestKey();
                }
            }
        }
    }

//    private Stack<BPlusTreeNode<?>> getPathStack(Integer key) {
//        // TODO check polymorphism
//        Stack<BPlusTreeNode<?>> pathStack = new Stack<>();
//        pathStack.push(this.getRootNode());
//        while (isInnerNode(pathStack.peek())) {
//            pathStack.push(getChild(pathStack.peek(), key));
//        }
//        return pathStack;
//    }

    private Stack<InnerNode> getPathStack(Integer key) {
        Stack<InnerNode> innerPathStack = new Stack<>();
        if (this.getRootNode().getHeight() != 0) {
            innerPathStack.push((InnerNode) this.getRootNode());
        } else {
            return innerPathStack;
        }
        while (innerPathStack.peek().getHeight() > 1) {
            innerPathStack.push((InnerNode) innerPathStack.peek().getChildNode(key));
        }
        return innerPathStack;
    }

    private int getIndexInArray(Integer[] array, int key) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == key) {
                return i;
            }
        }
        return -1;
    }


    @Nullable
    @Override
    public ValueReference insert(@NotNull Integer key, @NotNull ValueReference value) {
        LeafNode leafWithInsertion = this.getRootNode().findLeaf(key);

        if (getIndexInArray(leafWithInsertion.keys, key) != -1) {
            ValueReference oldValue = leafWithInsertion.references[getIndexInArray(leafWithInsertion.keys, key)];
            leafWithInsertion.references[getIndexInArray(leafWithInsertion.keys, key)] = value;
            return oldValue;
        }

        Stack<InnerNode> pathStack = getPathStack(key);

        if (leafWithInsertion.getEntries().count() < leafWithInsertion.n) {
            insertIntoNodeWithoutOverflow(leafWithInsertion, key, value);
            updateParents(key);
            return value;
        }
        return insertIntoLeafNodeWithOverflow(leafWithInsertion, key, value);

        //////// OLD ///////////
        // TODO check key exists? overwrite
        if (key == 0) {
            return null;
        } else {
            if (leafWithInsertion.getEntries().count() < leafWithInsertion.n) {
                // is platz, kein overflow
                // TODO Uncomment insertIntoNodeWithoutOverflow(leafWithInsertion, key, value);
                return value;
            }
            // TODO Uncomment Stack<BPlusTreeNode<?>> pathStack = getPathStack();
        }


        List<Entry> entryList = leafWithInsertion.getEntries().toList();

        Integer[] outKeyArray = new Integer[entryList.size() + 1];
        ValueReference[] outValueArray = new ValueReference[entryList.size() + 1];

        int inserted = 0;
        for (int i = 0; i < entryList.size(); i++) {
            if (entryList.get(i).getKey() > key && inserted == 0) {
                outKeyArray[i] = key;
                outValueArray[i] = value;
                inserted++;
            }
            outKeyArray[i + inserted] = entryList.get(i).getKey();
            outValueArray[i + inserted] = entryList.get(i).getValue();
        }
        // TODO Uncomment leafWithInsertion.keys = outKeyArray;
        // TODO Uncomment  leafWithInsertion.values = outValueArray;

//        for (int i = 0; i < leafWithInsertion.keys.length; i++) {
//            if (leafWithInsertion.keys[i] > key) {
//                leafWithInsertion.keys = {leafWithInsertion.keys[:i],key, leafWithInsertion.keys[i:]...};
//                leafWithInsertion.keys = new Integer[leafWithInsertion.keys.lenght + 1];
//            }
//        }
//        toInsertLeaf.


        return new ValueReference(10);
//        throw new UnsupportedOperationException("~~~ your implementation here ~~~");
        // Find LeafNode in which the key has to be inserted.
        //   It is a good idea to track the "path" to the LeafNode in a Stack or something alike.
        // Does the key already exist? Overwrite!
        //   leafNode.references[pos] = value;
        //   But remember return the old value!
        // New key - Is there still space?
        //   leafNode.keys[pos] = key;
        //   leafNode.references[pos] = value;
        //   Don't forget to update the parent keys and so on...
        // Otherwise
        //   Split the LeafNode in two!
        //   Is parent node root?
        //     update rootNode = ... // will have only one key
        //   Was node instanceof LeafNode?
        //     update parentNode.keys[?] = ...
        //   Don't forget to update the parent keys and so on...

        // Check out the exercise slides for a flow chart of this logic.
        // If you feel stuck, try to draw what you want to do and
        // check out Ex2Main for playing around with the tree by e.g. printing or debugging it.
        // Also check out all the methods on BPlusTreeNode and how they are implemented or
        // the tests in BPlusTreeNodeTests and BPlusTreeTests!
    }
}
