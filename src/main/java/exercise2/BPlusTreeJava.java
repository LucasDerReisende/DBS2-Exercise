package exercise2;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.exercise2.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This is the B+-Tree implementation you will work on.
 * Your task is to implement the insert-operation.
 */
@ChosenImplementation(true)
public class BPlusTreeJava extends AbstractBPlusTree {
    public BPlusTreeJava(int order) {
        super(order);
    }

    public BPlusTreeJava(BPlusTreeNode<?> rootNode) {
        super(rootNode);
    }

    private void insertIntoNodeWithoutOverflow(InnerNode node, Integer key, BPlusTreeNode<?> value) {
        for (int i = node.keys.length - 2; i >= 0; i--) {
            if (node.keys[i] == null) {
                continue;
            }
            if (node.keys[i] > key) {
                node.keys[i + 1] = node.keys[i];
                node.references[i + 2] = node.references[i + 1];
            } else {
                node.keys[i + 1] = key;
                node.references[i + 2] = value;
                return;
            }
        }
        node.keys[0] = key;
        node.references[1] = value;
    }

    private void insertIntoNodeWithoutOverflow(LeafNode node, Integer key, ValueReference value) {
        for (int i = node.keys.length - 2; i >= 0; i--) {
            if (node.keys[i] == null) {
                continue;
            }
            if (node.keys[i] > key) {
                node.keys[i + 1] = node.keys[i];
                node.references[i + 1] = node.references[i];
            } else {
                node.keys[i + 1] = key;
                node.references[i + 1] = value;
                return;
            }
        }
        node.keys[0] = key;
        node.references[0] = value;
    }

    private void insertIntoInnerNodeWithOverflow(InnerNode node, Integer key, BPlusTreeNode<?> value, Stack<InnerNode> innerNodeStack) {
        boolean isRootNode = this.rootNode.equals(node);
        int numberOfReferencesBefore = (int) Arrays.stream(node.references).takeWhile(Objects::nonNull).count();
        int numberOfKeysBefore = (int) Arrays.stream(node.keys).takeWhile(Objects::nonNull).count();

        int numberOfLeftKeys = (numberOfKeysBefore) / 2;
        int numberOfRightKeys = (numberOfKeysBefore + 1) / 2;

        int numberOfLeftReferences = numberOfLeftKeys + 1;
        int numberOfRightReferences = numberOfRightKeys + 1;

        int firstRightKey = node.keys[numberOfLeftKeys];
        int lastLeftKey = node.keys[numberOfLeftKeys - 1];
        InnerNode rightNode;
        Integer largestN1Key;
        if (key > firstRightKey) {
            // key is in the right node
            BPlusTreeNode<?>[] values = new BPlusTreeNode[numberOfRightReferences - 1];
            values[0] = node.references[numberOfLeftReferences];
            node.references[numberOfLeftReferences] = null;
            for (int i = numberOfLeftReferences + 1; i < numberOfReferencesBefore; i++) {
                values[i - numberOfLeftReferences] = node.references[i];
                node.keys[i - 1] = null;
                node.references[i] = null;
            }
            rightNode = new InnerNode(order, values);
            insertIntoNodeWithoutOverflow(rightNode, key, value);
            largestN1Key = node.keys[numberOfLeftKeys];
        }
        else {
            // key is in the left node
            BPlusTreeNode<?>[] values = new BPlusTreeNode[numberOfRightReferences];
            if (key > lastLeftKey) {
                // key is most right in left node
                values[0] = value;
                rightNode = fillRightNode(node, numberOfReferencesBefore, numberOfLeftReferences, values);
                largestN1Key = key;
            } else {
                values[0] = node.references[numberOfLeftReferences - 1];
                node.references[numberOfLeftReferences - 1] = null;
                rightNode = fillRightNode(node, numberOfReferencesBefore, numberOfLeftReferences, values);
                insertIntoNodeWithoutOverflow(node, key, value);
                largestN1Key = node.keys[numberOfLeftKeys];
            }
        }
        node.keys[numberOfLeftKeys] = null;

        if (checkNewRoot(node, isRootNode, rightNode)) return;

        InnerNode parentNode = innerNodeStack.pop();
        insertIntoInnerNode(parentNode, largestN1Key, rightNode, innerNodeStack);
    }

    @NotNull
    private InnerNode fillRightNode(InnerNode node, int numberOfReferencesBefore, int numberOfLeftReferences, BPlusTreeNode<?>[] values) {
        InnerNode rightNode;
        for (int i = numberOfLeftReferences; i < numberOfReferencesBefore; i++) {
            values[i - numberOfLeftReferences + 1] = node.references[i];
            node.keys[i - 1] = null;
            node.references[i] = null;
        }
        rightNode = new InnerNode(order, values);
        return rightNode;
    }

    private void insertIntoInnerNode(InnerNode node, Integer key, BPlusTreeNode<?> value, Stack<InnerNode> innerNodeStack) {
        if (Arrays.stream(node.keys).takeWhile(Objects::nonNull).count() < node.n) {
            insertIntoNodeWithoutOverflow(node, key, value);
            updateParents(key);
            return;
        }
        insertIntoInnerNodeWithOverflow(node, key, value, innerNodeStack);
    }

    private void insertIntoLeafNodeWithOverflow(LeafNode node, Integer key, ValueReference value) {
        Stack<InnerNode> innerNodeStack = getPathStack(key);
        boolean isRootNode = this.rootNode.equals(node);

        int beforeCount = (int) node.getEntries().count();
        // firstCount is the number of entries in the left node after split
        // +2 -> +1 for additional element, +1 for correct division
        int firstCount = (beforeCount + 2) / 2;

        LeafNode rightNode;
        if (key > node.keys[firstCount - 1]) {
            // new value is in new node
            Entry[] entries = new Entry[beforeCount - firstCount];
            for (int i = firstCount; i < beforeCount; i++) {
                entries[i - firstCount] = new Entry(node.keys[i], node.references[i]);
                node.keys[i] = null;
                node.references[i] = null;
            }
            rightNode = new LeafNode(node.order, entries);
            insertIntoNodeWithoutOverflow(rightNode, key, value);
        } else {
            // new value is in old node
            Entry[] entries = new Entry[beforeCount + 1 - firstCount];
            for (int i = firstCount - 1; i < beforeCount; i++) {
                entries[i - (firstCount - 1)] = new Entry(node.keys[i], node.references[i]);
                node.keys[i] = null;
                node.references[i] = null;
            }
            rightNode = new LeafNode(node.order, entries);
            insertIntoNodeWithoutOverflow(node, key, value);
        }

        if (node.getClass() == InitialRootNode.class) {
            Entry[] entries1 = new Entry[node.getNodeSize()];
            for (int i = 0; i < node.getNodeSize(); i++) {
                entries1[i] = new Entry(node.keys[i], node.references[i]);
            }
            node = new LeafNode(node.order, entries1);
        }

        rightNode.nextSibling = node.nextSibling;
        node.nextSibling = rightNode;

        if (checkNewRoot(node, isRootNode, rightNode)) return;

        // ist blatt
        /* insert gemäß obiger Methode*/
        InnerNode parentNode = innerNodeStack.pop();
        Integer smallestN2Key = rightNode.getSmallestKey();
        insertIntoInnerNode(parentNode, smallestN2Key, rightNode, innerNodeStack);
    }

    private boolean checkNewRoot(BPlusTreeNode<?> node, boolean isRootNode, BPlusTreeNode<?> nextNode) {
        if (isRootNode) {
            // create new root
            // set node and nextNode as children
            BPlusTreeNode<?>[] nodes = {node, nextNode};
            this.rootNode = new InnerNode(node.order, nodes);
            return true;
        }
        return false;
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


    private Stack<InnerNode> getPathStack(Integer key) {
        Stack<InnerNode> innerPathStack = new Stack<>();
        if (this.getRootNode().getHeight() != 0) {
            innerPathStack.push((InnerNode) this.getRootNode());
        } else {
            return innerPathStack;
        }
        while (innerPathStack.peek().getHeight() > 1) {
            innerPathStack.push((InnerNode) innerPathStack.peek().selectChild(key));
        }
        return innerPathStack;
    }

    private int getIndexInArray(Integer[] array, Integer key) {
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], key)) {
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

        if (leafWithInsertion.getEntries().count() < leafWithInsertion.n) {
            insertIntoNodeWithoutOverflow(leafWithInsertion, key, value);
            updateParents(key);
            return null;
        }
        insertIntoLeafNodeWithOverflow(leafWithInsertion, key, value);
        return null;

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
