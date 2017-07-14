
package soundtest;

public class Result {

    
    private String output;
    private String path;
    private double value;
    public Result() {}
    public Result(String s,String path, double value)
    {
        this.output = s;
        this.value = value;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
