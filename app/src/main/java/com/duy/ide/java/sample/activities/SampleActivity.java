package com.duy.ide.java.sample.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.java.sample.fragments.SelectCategoryFragment;
import com.duy.ide.java.sample.fragments.SelectProjectFragment;
import com.duy.ide.java.sample.model.AssetUtil;
import com.duy.ide.java.sample.model.CodeCategory;
import com.duy.ide.java.sample.model.CodeProjectSample;
import com.duy.ide.file.FileManager;
import com.duy.android.compiler.file.java.JavaProject;
import com.duy.projectview.ProjectManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SampleActivity extends AbstractAppCompatActivity implements
        SelectCategoryFragment.CategoryClickListener, SelectProjectFragment.ProjectClickListener {
    public static final String PROJECT_FILE = "project_file";
    private static final String TAG = "SampleActivity";
    private CodeCategory category = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle(R.string.code_sample);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SelectCategoryFragment fragment = null;
        if (savedInstanceState != null) {
            fragment = (SelectCategoryFragment)
                    getSupportFragmentManager().findFragmentByTag(SelectCategoryFragment.TAG);
        }
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            fragment = SelectCategoryFragment.newInstance(getCodeCategories());
        }
        fm.replace(R.id.content, fragment, SelectCategoryFragment.TAG).commit();
    }

    private ArrayList<CodeCategory> getCodeCategories() {
        AssetManager assets = getAssets();
        ArrayList<CodeCategory> codeSamples = new ArrayList<>();
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document parse = documentBuilder.parse(assets.open("sample/package.xml"));
            Element root = parse.getDocumentElement();
            NodeList categories = root.getElementsByTagName("category");
            for (int i = 0; i < categories.getLength(); i++) {
                Element category = (Element) categories.item(i);
                String categoryName = category.getAttribute("name");
                String categoryDescription = category.getAttribute("description");
                String headerImage = category.getAttribute("image");
                String categoryPath = category.getAttribute("path");
                CodeCategory codeCategory = new CodeCategory(categoryName, categoryDescription,
                        headerImage, categoryPath);
                NodeList projects = category.getElementsByTagName("project");
                for (int j = 0; j < projects.getLength(); j++) {
                    Element project = (Element) projects.item(j);
                    String projectName = project.getAttribute("name");
                    String projectPath = project.getAttribute("path");
                    String projectDesc = project.getAttribute("description");
                    CodeProjectSample entry = new CodeProjectSample(projectName, projectPath, projectDesc);
                    Log.d(TAG, "getCategories entry = " + entry);
                    codeCategory.addCodeItem(entry);
                }
                codeSamples.add(codeCategory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return codeSamples;
    }

    @Override
    public void onCategoryClick(CodeCategory category) {
        this.category = category;
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.add(R.id.content, SelectProjectFragment.newInstance(category), SelectProjectFragment.TAG).commit();
        fm.addToBackStack(null);
    }


    @Override
    public void onProjectClick(final CodeProjectSample codeProjectSample) {
        final String projectName = codeProjectSample.getName();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("This action will be create new project");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File out = new File(FileManager.EXTERNAL_DIR, projectName);
                int count = 1;
                while (out.exists()) {
                    out = new File(FileManager.EXTERNAL_DIR, projectName + count);
                    count++;
                }
                boolean success = AssetUtil.copyAssetFolder(getAssets(),
                        codeProjectSample.getPath(),
                        out.getPath());
                if (success) {
                    JavaProject pf = ProjectManager.createProjectIfNeed(getApplicationContext(), out);
                    Intent intent = getIntent();
                    intent.putExtra(PROJECT_FILE, pf);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(SampleActivity.this, "Can not copy project!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();

    }
}
