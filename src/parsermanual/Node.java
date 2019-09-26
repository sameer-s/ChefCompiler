package parsermanual;

public class Node {
    public TokenManual token;
    public Node left = null, right = null;

    public Node(TokenManual token) {
        this.token = token;
    }
}
