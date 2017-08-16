package simpleapp.wheretobuy.models;

public class AutoCompleteResult {

    private String type;
    private String name;
    private String id;

    public AutoCompleteResult(String type, String name, String id) {
        this.type = type;
        this.name = name;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().isInstance(this)) {
            AutoCompleteResult obj2 = (AutoCompleteResult) obj;
            if (this.getName().toLowerCase().equals(obj2.getName().toLowerCase())){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}