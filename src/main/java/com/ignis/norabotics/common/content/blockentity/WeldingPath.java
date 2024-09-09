package com.ignis.norabotics.common.content.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeldingPath extends ArrayList<Vec3> {

    private Direction direction = Direction.NORTH;
    private final float[] intervals;
    private float totalLength;

    protected WeldingPath(List<Vec3> vecs) {
        intervals = new float[vecs.size()];
        Vec3 prev = null;
        for(Vec3 vec : vecs) {
            if(prev != null) totalLength += (float) prev.distanceTo(vec);
            add(vec);
            prev = vec;
        }
        intervals[0] = 0;
        for(int i = 1; i < intervals.length - 1; i++) {
            double dist = vecs.get(i).distanceTo(vecs.get(i - 1));
            intervals[i] = intervals[i - 1] + (float) (dist / totalLength);
        }
        intervals[intervals.length - 1] = 1;
    }

    private double frac(long time, long startTime) {
        return (double) (time - startTime) / (getLength() * 100d);
    }

    public boolean isFinished(long time, long startTime) {
        double frac = frac(time, startTime);
        return frac >= 1;
    }

    public Vec3 lerp(long time, long startTime) {
        return lerp(frac(time, startTime));
    }

    public Vec3 lerp(double fraction) {
        fraction = Mth.clamp(fraction, 0, 1);
        int i = 0;
        while(intervals[i] < fraction) {
            i += 1;
        }
        return get(i - 1).lerp(get(i), (fraction - intervals[i - 1]) / (intervals[i] - intervals[i - 1]));
    }

    public WeldingPath offset(BlockPos pos) {
        WeldingPath copy = clone();
        copy.replaceAll(vec3 -> vec3.add(Vec3.atBottomCenterOf(pos)));
        return copy;
    }

    public WeldingPath rotateToDirection(Direction dir) {
        WeldingPath copy = clone();
        for(int i = 0; i < size(); i++) {
            Vec3 vec = get(i);
            // Undo previous rotation
            Quaternionf upwardFacing = direction.getRotation().invert().mul((float) vec.x, (float) vec.y, (float) vec.z, 0).mul(direction.getRotation());
            // Apply new rotation
            Quaternionf newFacing = dir.getRotation().mul(upwardFacing).mul(dir.getRotation().invert());
            copy.set(i, new Vec3(newFacing.x, newFacing.y, newFacing.z));
        }
        copy.direction = dir;
        return copy;
    }

    public float getLength() {
        return totalLength;
    }

    public static WeldingPath of(Vec3... bones) {
        return new WeldingPath(Arrays.asList(bones));
    }

    @Override
    public WeldingPath clone() {
        return WeldingPath.of(stream().map(vec -> new Vec3(vec.x, vec.y, vec.z)).toList().toArray(new Vec3[size()]));
    }
}
