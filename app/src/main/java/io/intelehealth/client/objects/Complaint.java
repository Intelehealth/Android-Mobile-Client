
package io.intelehealth.client.objects;

/**
 *This class contains get() and set() methods for complaint's details.
 * Created by tusharjois
 */

public class Complaint {

    public Complaint() {
    }

    public Complaint(String n, String d) {
        this.name = n;
        this.data = d;
    }

    private String name;

    private String data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

