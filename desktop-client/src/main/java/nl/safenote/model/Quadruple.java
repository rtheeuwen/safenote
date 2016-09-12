package nl.safenote.model;


public final class Quadruple<A, B, C, D> {

    private final A a;
    private final B b;
    private final C c;
    private final D d;

    public Quadruple(A a, B b, C c, D d) {
        if(a==null||b==null||c==null||d==null)
            throw new IllegalArgumentException("Elements cannot be null");
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    public D getD() {
        return d;
    }
}
