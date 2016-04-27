package com.cryptoVerifier;

/**
 * Created by ori on 27/03/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ori.verifier.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parametersMain.ParametersMain;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private Map<String, List<String>> laptopCollections;
    private List<String> laptops;


    //for images
    private boolean CHECKIMAGESFORDEBUG = false;
    private HashMap<String, Drawable> nameToImgMap;
    //end of images


    public ExpandableListAdapter(Activity context, List<String> laptops,
                                 Map<String, List<String>> laptopCollections) {
        this.context = context;
        this.laptopCollections = laptopCollections;
        this.laptops = laptops;

        //for images
        String adminJson = ParametersMain.theAdminJson;
        String delims = "[:,\"]";
        String[] tokens = adminJson.split(delims);
        nameToImgMap = tokensToImages(tokens);

        //end of images
    }

    //for images
    private HashMap<String, Drawable> tokensToImages(String[] tokens) {

        HashMap<String, Drawable> nameToImgMap = new HashMap<>();
        boolean sawName = false, sawCand = false, sawImage = false;
        String name = "";
        Drawable drawable;
        String filePrefix = "gui_";

        for (int i = 0; i < tokens.length; i++) {
            String s = tokens[i];
            if (!sawName) {
                if (s.equals("name")) {
                    sawName = true;
                }
            } else if (!sawCand) {
                if (s.trim().equals("")) continue;
                name = s;
                sawCand = true;
            } else if (!sawImage) {
                if (s.equals("image")) {
                    sawImage = true;
                }
            } else {
                if (s.trim().equals("")) continue;
                s = trimJPG(s);
                int id = context.getResources().getIdentifier(filePrefix + s, "drawable", context.getPackageName());
                try {
                    drawable = context.getResources().getDrawable(id);
                } catch (Resources.NotFoundException rnfe) {
                    rnfe.printStackTrace();
                    int id1 = context.getResources().getIdentifier("parties", "drawable", context.getPackageName());
                    drawable = context.getResources().getDrawable(id1);
                }
                if (drawable != null) {
                    nameToImgMap.put(name, drawable);
                }
                sawImage = false;
                sawName = false;
                sawCand = false;
            }

        }
        return nameToImgMap;
    }

    private String trimJPG(String s) {
        String delims = "[.]";
        String[] tokens = s.split(delims);
        return tokens[0];
    }
    //end of images

    public Object getChild(int groupPosition, int childPosition) {
        return laptopCollections.get(laptops.get(groupPosition)).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String laptop = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_item, null);
        }

        TextView item = (TextView) convertView.findViewById(R.id.child_textView);

        ImageView delete = (ImageView) convertView.findViewById(R.id.parties_image);

        //for images

        Drawable drawable = nameToImgMap.get(laptop);
        if (drawable != null) {
            delete.setImageDrawable(drawable);
        }

        if (CHECKIMAGESFORDEBUG) {
            if (laptop.equals("יאיר")) {
                int id = context.getResources().getIdentifier("gui_israel_beytenu", "drawable", context.getPackageName());
                drawable = context.getResources().getDrawable(id);
                delete.setImageDrawable(drawable);
            }
            if (laptop.equals("זהבוש")) {
                int id = context.getResources().getIdentifier("gui_obama", "drawable", context.getPackageName());
                drawable = context.getResources().getDrawable(id);
                delete.setImageDrawable(drawable);
            }
            if (laptop.equals("DaniDanon3")) {
                int id = context.getResources().getIdentifier("gui_obama2", "drawable", context.getPackageName());
                drawable = context.getResources().getDrawable(id);
                delete.setImageDrawable(drawable);
            }
            if (laptop.equals("DaniDanon")) {
                int id = context.getResources().getIdentifier("gui_yesh_atid", "drawable", context.getPackageName());
                drawable = context.getResources().getDrawable(id);
                delete.setImageDrawable(drawable);
            }
            if (laptop.equals("ביבי")) {
                int id = context.getResources().getIdentifier("gui_hahavoda", "drawable", context.getPackageName());
                drawable = context.getResources().getDrawable(id);
                delete.setImageDrawable(drawable);
            }
            if (laptop.equals("DaniDanon2")) {
                int id = context.getResources().getIdentifier("gui_meretz", "drawable", context.getPackageName());
                drawable = context.getResources().getDrawable(id);
                delete.setImageDrawable(drawable);
            }
        }
        //end of images

        /*delete.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to remove?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                List<String> child =
                                        laptopCollections.get(laptops.get(groupPosition));
                                child.remove(childPosition);
                                notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        */
        item.setText(laptop);
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        return laptopCollections.get(laptops.get(groupPosition)).size();
    }

    public Object getGroup(int groupPosition) {
        return laptops.get(groupPosition);
    }

    public int getGroupCount() {
        return laptops.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String laptopName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_item,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.group_textView);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(laptopName);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}