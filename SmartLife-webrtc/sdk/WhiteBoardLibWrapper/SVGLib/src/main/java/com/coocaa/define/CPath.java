package com.coocaa.define;

import android.graphics.PointF;
import android.util.Log;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * android点击事件处理成
 * @Author: yuzhan
 */
public class CPath implements Serializable {
    private PointF start = new PointF();
    private List<PointF> move = new LinkedList<>();
    private PointF end = new PointF();
    private Queue<PointF> recycledPoint = new LinkedList<>();

    public void reset() {
        start.set(0f, 0f);
        end.set(0f, 0f);
        recycledPoint.addAll(move);
        move.clear();
    }

    public void start(float x, float y) {
        start.set(x, y);
    }

    public void end(float x, float y) {
        end.set(x, y);
    }

    public void moveTo(float x, float y) {
        PointF p = recycledPoint.isEmpty() ? new PointF() : recycledPoint.poll();
        p.set(x, y);
        move.add(p);
    }

    public PointF getStart() {
        return start;
    }

    public List<PointF> getMove() {
        return move;
    }

    public PointF getEnd() {
        return end;
    }

    public boolean isEmpty() {
        return start.length() == 0 && end.length() == 0 && move.size()==0;
    }
}
