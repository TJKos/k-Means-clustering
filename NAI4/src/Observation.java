import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Observation {
    List<Double> vec;
    int group;

    public Observation(int group, List<Double> vec) {
        this.vec = vec;
        this.group = group;
    }

    public Observation(int group, Double... doubles) {
        this.vec = Arrays.stream(doubles).collect(Collectors.toList());
        this.group = group;
    }

    public double getDistance(List<Double> vec2){
        double distanceSquared = 0;

        for (int i = 0; i < vec2.size(); i++) {
//            distanceSquared += ((vec.get(i) - vec2.get(i)) * (vec.get(i) - vec2.get(i)));
            distanceSquared += Math.pow(vec.get(i) - vec2.get(i), 2);
        }


        return Math.sqrt(distanceSquared);
    }

    public void addVector(List<Double> vec){
        for (int i = 0; i < vec.size(); i++) {
            this.vec.set(i, this.vec.get(i) + vec.get(i));
        }
    }

    public void divideVector(double d){
        for (int i = 0; i < vec.size(); i++) {
            this.vec.set(i, this.vec.get(i) / d);
        }
    }

    @Override
    public String toString() {
        return "Group: " + group + ", vec: " + vec;
    }
}
