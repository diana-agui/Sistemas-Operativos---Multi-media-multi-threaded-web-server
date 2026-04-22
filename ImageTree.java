public class ImageTree {
    Node root;

    // --- Insert by image ID ---
    public void insert(Image image) {
        if (containsDuplicate(root, image.id))
            throw new IllegalArgumentException("Image with ID " + image.id + " already exists.");
        root = insertRecursive(root, image);
    }

    private Node insertRecursive(Node node, Image image) {
        if (node == null) return new Node(image);
        if (image.id < node.image.id)
            node.left = insertRecursive(node.left, image);
        else if (image.id > node.image.id)
            node.right = insertRecursive(node.right, image);
        return node;
    }

    private boolean containsDuplicate(Node node, int id) {
        if (node == null) return false;
        if (node.image.id == id) return true;
        return id < node.image.id
                ? containsDuplicate(node.left, id)
                : containsDuplicate(node.right, id);
    }

    // --- Search by ID --- O(log n)
    public Image searchById(int id) {
        return searchByIdRecursive(root, id);
    }

    private Image searchByIdRecursive(Node node, int id) {
        if (node == null) return null;           // not found
        if (node.image.id == id) return node.image;
        return id < node.image.id
                ? searchByIdRecursive(node.left, id)
                : searchByIdRecursive(node.right, id);
    }
    public void listAll() {
        System.out.println("=== Image Directory ===");
        listAllRecursive(root);
    }

    private void listAllRecursive(Node node) {
        if (node == null) return;
        listAllRecursive(node.left);
        System.out.println(node.image);
        listAllRecursive(node.right);
    }
}
