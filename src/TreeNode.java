import java.util.Objects;

public class TreeNode {
    private int key;
    TreeNode left;
    TreeNode right;
    public TreeNode(int num) {
        this.key = num;
        left = null;
        right = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode treeNode = (TreeNode) o;
        return key == treeNode.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
