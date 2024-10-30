class Order {
    private int id;
    private int customerId;
    private double total;

    public Order(int customerId, double total) {
        this.customerId = customerId;
        this.total = total;
    }

    // Getters and setters...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", customerId=" + customerId + ", total=" + total + '}';
    }
}