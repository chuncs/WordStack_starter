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

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 3;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    //private Stack<LetterTile> placedTiles = new Stack<>();
    private Stack<Pair<LetterTile, ViewGroup>> placedTiles = new Stack<>();
    //private int undoIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if (word.length() == WORD_LENGTH) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }

                //placedTiles.push(tile);

                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    ViewGroup parent = (ViewGroup) tile.getParent();
                    /*if (parent instanceof StackedLayout) {
                        undoIndex = 0;
                    } else {
                        undoIndex = tile.getIndex().peek();
                    }*/
                    //tile.setIndex(undoIndex);
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty() && checkWin()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }

                    //placedTiles.push(tile);
                    //ViewGroup word1Layout = (ViewGroup) findViewById(R.id.word1);
                    //ViewGroup word2Layout = (ViewGroup) findViewById(R.id.word2);
                    tile.setIndex(((ViewGroup) v).getChildCount() - 1);
                    if (parent instanceof StackedLayout) {
                        placedTiles.add(Pair.create(tile, (ViewGroup) stackedLayout));
                    } else {
                        /*if (v.getId() == R.id.word1) {
                            placedTiles.add(Pair.create(tile, word2Layout));
                            updateViewAfterMove(word2Layout, word1Layout, tile, undoIndex);
                        } else if (v.getId() == R.id.word2) {
                            placedTiles.add(Pair.create(tile, word1Layout));
                            updateViewAfterMove(word1Layout, word2Layout, tile, undoIndex);
                        }*/
                        placedTiles.add(Pair.create(tile, parent));
                        //updateViewAfterMove(parent, (ViewGroup) v, tile, undoIndex);
                        updateViewAfterMove();
                    }

                    return true;
            }
            return false;
        }
    }

    private boolean checkWin() {
        ViewGroup word1Layout = (ViewGroup) findViewById(R.id.word1);
        ViewGroup word2Layout = (ViewGroup) findViewById(R.id.word2);
        String word1View = "", word2View = "";

        for (int i = 0; i < word1Layout.getChildCount(); i++) {
            word1View += ((LetterTile) word1Layout.getChildAt(i)).getLetter();
        }
        for (int i = 0; i < word2Layout.getChildCount(); i++) {
            word2View += ((LetterTile) word2Layout.getChildAt(i)).getLetter();
        }

        if (word1View.length() == WORD_LENGTH && word2View.length() == WORD_LENGTH) {
            if (word1View.equals(word1) && word2View.equals(word2)) {
                Toast.makeText(MainActivity.this, "You got it!", Toast.LENGTH_LONG).show();
                return true;
            }
            if (words.contains(word1View) && words.contains(word2View)) {
                Toast.makeText(MainActivity.this, "You nailed it!", Toast.LENGTH_LONG).show();
                return true;
            }
        }

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Try again.");
        return false;
    }

    //buggy update method
    private void updateViewAfterMove(ViewGroup sourceView, ViewGroup targetView, LetterTile currentTile, int indexBefore) {
        for (int i = indexBefore; i < sourceView.getChildCount(); i++) {
            Stack<Integer> sourceIndex = ((LetterTile) sourceView.getChildAt(i)).getIndex();
            int poppedIndex = sourceIndex.pop();
            sourceIndex.push(poppedIndex - 1);
        }

        int currentIndex = currentTile.getIndex().peek();
        for (int i = currentIndex + 1; i < targetView.getChildCount(); i++) {
            Stack<Integer> targetIndex = ((LetterTile) targetView.getChildAt(i)).getIndex();
            int poppedIndex = targetIndex.pop();
            targetIndex.push(poppedIndex + 1);
        }
    }

    //new update method
    private void updateViewAfterMove() {
        ViewGroup word1LinearLayout = (ViewGroup) findViewById(R.id.word1);
        ViewGroup word2LinearLayout = (ViewGroup) findViewById(R.id.word2);

        for(int i = 0; i < word1LinearLayout.getChildCount(); i++) {
            Stack<Integer> sourceIndex = ((LetterTile) word1LinearLayout.getChildAt(i)).getIndex();
            sourceIndex.pop();
            sourceIndex.push(i);
        }

        for(int i = 0; i < word2LinearLayout.getChildCount(); i++) {
            Stack<Integer> sourceIndex = ((LetterTile) word2LinearLayout.getChildAt(i)).getIndex();
            sourceIndex.pop();
            sourceIndex.push(i);
        }
    }

    public boolean onStartGame(View view) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        word1 = words.get(random.nextInt(words.size()));
        word2 = words.get(random.nextInt(words.size()));
        String word3 = "";
        String reversedWord3 = "";
        int word1Counter = 0, word2Counter = 0;
        ViewGroup word1LinearLayout = (ViewGroup) findViewById(R.id.word1);
        ViewGroup word2LinearLayout = (ViewGroup) findViewById(R.id.word2);

        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();
        stackedLayout.clear();
        placedTiles.clear();

        while (word1Counter < WORD_LENGTH || word2Counter < WORD_LENGTH) {
            if (word1Counter == WORD_LENGTH) {
                word3 += word2.substring(word2Counter);
                break;
            } else if (word2Counter == WORD_LENGTH) {
                word3 += word1.substring(word1Counter);
                break;
            }

            if (random.nextInt(2) == 0 && word1Counter < WORD_LENGTH) {
                word3 += word1.charAt(word1Counter);
                word1Counter++;
            } else {
                word3 += word2.charAt(word2Counter);
                word2Counter++;
            }
        }

        messageBox.setText(word3);
        reversedWord3 = new StringBuilder(word3).reverse().toString();
        for (char letter : reversedWord3.toCharArray()) {
            stackedLayout.push(new LetterTile(this, letter));
        }

        return true;
    }

    public boolean onUndo(View view) {
        if (!placedTiles.empty()) {
            //placedTiles.pop().moveToViewGroup(stackedLayout);
            Pair<LetterTile, ViewGroup> popped = placedTiles.pop();
            //int indexBefore = popped.first.getIndex().peek();
            //ViewGroup parent = (ViewGroup) popped.first.getParent();
            popped.first.undoViewGroup(popped.second);

            /*ViewGroup word1Layout = (ViewGroup) findViewById(R.id.word1);
            ViewGroup word2Layout = (ViewGroup) findViewById(R.id.word2);
            if (popped.second.getId() == R.id.word1) {
                updateViewAfterMove(word2Layout, word1Layout, popped.first, indexBefore);
            }
            if (popped.second.getId() == R.id.word2) {
                updateViewAfterMove(word1Layout, word2Layout, popped.first, indexBefore);
            }*/

            if (!(popped.second instanceof StackedLayout)) {
                //updateViewAfterMove(parent, popped.second, popped.first, indexBefore);
                updateViewAfterMove();
            }
        }

        return true;
    }
}
