/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Stack;

public class LetterTile extends TextView {

    public static final int TILE_SIZE = 100;
    private Character letter;
    private boolean frozen;
    private Stack<Integer> index;

    public LetterTile(Context context, Character letter) {
        super(context);
        this.letter = letter;
        index = new Stack<>();
        setText(letter.toString());
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setHeight(TILE_SIZE);
        setWidth(TILE_SIZE);
        setTextSize(30);
        setBackgroundColor(Color.rgb(255, 255, 200));
    }

    public Character getLetter() {
        return letter;
    }

    public Stack<Integer> getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index.push(index);
    }

    public void moveToViewGroup(ViewGroup targetView) {
        ViewParent parent = getParent();
        if (parent instanceof StackedLayout ) {
            StackedLayout owner = (StackedLayout) parent;
            owner.pop();
            targetView.addView(this);
            freeze();
            setVisibility(View.VISIBLE);
        } else {
            ViewGroup owner = (ViewGroup) parent;
            owner.removeView(this);
            targetView.addView(this);
            /*if (owner.getId() == R.id.word1 || owner.getId() == R.id.word2) {
                targetView.addView(this);
            } else {
                ((StackedLayout) targetView).push(this);
            }*/
            unfreeze();
        }
    }

    public void freeze() {
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            return startDrag(ClipData.newPlainText("", ""), new View.DragShadowBuilder(this), this, 0);
        }
        return super.onTouchEvent(motionEvent);
    }

    public void undoViewGroup(ViewGroup targetView) {
        ViewGroup owner = (ViewGroup) getParent();
        owner.removeView(this);
        index.pop();
        if (targetView.getId() == R.id.word1 || targetView.getId() == R.id.word2) {
            targetView.addView(this, index.peek());
        } else {
            ((StackedLayout) targetView).push(this);
        }
    }
}
