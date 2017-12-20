package com.duy.ide.autocomplete.autocomplete;

import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;

import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Created by Duy on 15-Aug-17.
 */
public class TestJavacParser extends TestCase {
    private String src = "package com.duy.project.view.dialog;\n" +
            "\n" +
            "import android.app.Activity;\n" +
            "import android.app.Dialog;\n" +
            "import android.content.Context;\n" +
            "import android.os.AsyncTask;\n" +
            "import android.os.Bundle;\n" +
            "import android.support.annotation.NonNull;\n" +
            "import android.support.annotation.Nullable;\n" +
            "import android.support.design.widget.TextInputLayout;\n" +
            "import android.support.v4.widget.SwipeRefreshLayout;\n" +
            "import android.support.v7.app.AlertDialog;\n" +
            "import android.support.v7.app.AppCompatDialogFragment;\n" +
            "import android.support.v7.widget.DividerItemDecoration;\n" +
            "import android.support.v7.widget.LinearLayoutManager;\n" +
            "import android.support.v7.widget.RecyclerView;\n" +
            "import android.text.TextUtils;\n" +
            "import android.view.LayoutInflater;\n" +
            "import android.view.View;\n" +
            "import android.view.ViewGroup;\n" +
            "import android.view.Window;\n" +
            "import android.view.WindowManager;\n" +
            "import android.widget.Button;\n" +
            "import android.widget.EditText;\n" +
            "import android.widget.TextView;\n" +
            "import android.widget.Toast;\n" +
            "\n" +
            "import com.duy.ide.R;\n" +
            "import com.duy.ide.file.FileManager;\n" +
            "import com.duy.ide.file.FileSelectListener;\n" +
            "import com.duy.ide.file.PreferenceHelper;\n" +
            "import com.duy.ide.file.adapter.FileAdapterListener;\n" +
            "import com.duy.ide.file.adapter.FileDetail;\n" +
            "import com.duy.ide.file.adapter.FileListAdapter;\n" +
            "import com.duy.ide.utils.Build;\n" +
            "\n" +
            "import org.apache.commons.io.FileUtils;\n" +
            "import org.apache.commons.io.FilenameUtils;\n" +
            "\n" +
            "import java.io.File;\n" +
            "import java.text.SimpleDateFormat;\n" +
            "import java.util.LinkedList;\n" +
            "import java.util.Locale;\n" +
            "\n" +
            "/**\n" +
            " * Created by Duy on 17-Jul-17.\n" +
            " */\n" +
            "\n" +
            "public class DialogSelectDirectory extends AppCompatDialogFragment implements View.OnClickListener, View.OnLongClickListener,\n" +
            "        SwipeRefreshLayout.OnRefreshListener, FileAdapterListener {\n" +
            "    public static final String TAG = \"DialogSelectDirectory\";\n" +
            "\n" +
            "    private FileSelectListener listener;\n" +
            "    private RecyclerView listFiles;\n" +
            "    private Activity activity;\n" +
            "    private String currentFolder;\n" +
            "    private boolean wantAFile = false;\n" +
            "    private SwipeRefreshLayout swipeRefreshLayout;\n" +
            "    @Nullable\n" +
            "    private FileListAdapter mAdapter;\n" +
            "    private TextView txtPath;\n" +
            "    private int request;\n" +
            "\n" +
            "    public static DialogSelectDirectory newInstance(String path, int request) {\n" +
            "\n" +
            "        Bundle args = new Bundle();\n" +
            "        args.putString(\"path\", path);\n" +
            "        args.putInt(\"request\", request);\n" +
            "        DialogSelectDirectory fragment = new DialogSelectDirectory();\n" +
            "        fragment.setArguments(args);\n" +
            "        return fragment;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onResume() {\n" +
            "        super.onResume();\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onAttach(Context context) {\n" +
            "        super.onAttach(context);\n" +
            "        try {\n" +
            "            listener = (FileSelectListener) getActivity();\n" +
            "        } catch (Exception ignored) {\n" +
            "\n" +
            "        }\n" +
            "        activity = getActivity();\n" +
            "        request = getArguments().getInt(\"request\");\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onStart() {\n" +
            "        super.onStart();\n" +
            "        Dialog dialog = getDialog();\n" +
            "        if (dialog != null) {\n" +
            "            Window window = dialog.getWindow();\n" +
            "            if (window != null) window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,\n" +
            "                    WindowManager.LayoutParams.MATCH_PARENT);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    @Nullable\n" +
            "    @Override\n" +
            "    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {\n" +
            "        return inflater.inflate(R.layout.dialog_choose_file, container, false);\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {\n" +
            "        super.onViewCreated(view, savedInstanceState);\n" +
            "\n" +
            "        currentFolder = getArguments().getString(\"path\", FileManager.EXTERNAL_DIR);\n" +
            "        wantAFile = false;\n" +
            "\n" +
            "        bindView(view);\n" +
            "\n" +
            "        //load file\n" +
            "        new UpdateList(currentFolder).execute();\n" +
            "    }\n" +
            "\n" +
            "    private void bindView(View view) {\n" +
            "        listFiles = view.findViewById(R.id.list_file);\n" +
            "        listFiles.setHasFixedSize(true);\n" +
            "        listFiles.setLayoutManager(new LinearLayoutManager(activity));\n" +
            "        listFiles.addItemDecoration(new DividerItemDecoration(getContext(),\n" +
            "                DividerItemDecoration.VERTICAL));\n" +
            "\n" +
            "        swipeRefreshLayout = view.findViewById(R.id.refresh_view);\n" +
            "        swipeRefreshLayout.setOnRefreshListener(this);\n" +
            "\n" +
            "        view.findViewById(R.id.action_new_folder).setOnClickListener(this);\n" +
            "        txtPath = view.findViewById(R.id.txt_path);\n" +
            "        view.findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {\n" +
            "            @Override\n" +
            "            public void onClick(View view) {\n" +
            "                if (listener != null) {\n" +
            "                    listener.onFileSelected(new File(currentFolder), request);\n" +
            "                    dismiss();\n" +
            "                }\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    private void createNewFolder() {\n" +
            "        AlertDialog.Builder builder = new AlertDialog.Builder(activity);\n" +
            "        builder.setTitle(R.string.new_folder);\n" +
            "        builder.setView(R.layout.dialog_new_file);\n" +
            "        final AlertDialog alertDialog = builder.create();\n" +
            "        alertDialog.show();\n" +
            "        final EditText editText = (EditText) alertDialog.findViewById(R.id.edit_file_name);\n" +
            "        final TextInputLayout textInputLayout = (TextInputLayout) alertDialog.findViewById(R.id.hint);\n" +
            "        assert textInputLayout != null;\n" +
            "        textInputLayout.setHint(getString(R.string.enter_new_folder_name));\n" +
            "        Button btnOK = null;\n" +
            "//        btnOK = (Button) alertDialog.findViewById(R.id.btn_ok);\n" +
            "        Button btnCancel = (Button) alertDialog.findViewById(R.id.btn_cancel);\n" +
            "        btnCancel.setOnClickListener(new View.OnClickListener() {\n" +
            "            @Override\n" +
            "            public void onClick(View v) {\n" +
            "                alertDialog.cancel();\n" +
            "            }\n" +
            "        });\n" +
            "        btnOK.setOnClickListener(new View.OnClickListener() {\n" +
            "            @Override\n" +
            "            public void onClick(View v) {\n" +
            "                //get string path of in edit text\n" +
            "                String fileName = editText != null ? editText.getText().toString() : null;\n" +
            "                if (fileName.isEmpty()) {\n" +
            "                    editText.setError(getString(R.string.enter_new_file_name));\n" +
            "                    return;\n" +
            "                }\n" +
            "                //create new file\n" +
            "                File file = new File(currentFolder, fileName);\n" +
            "                file.mkdirs();\n" +
            "                new UpdateList(currentFolder).execute();\n" +
            "                alertDialog.cancel();\n" +
            "            }\n" +
            "        });\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onClick(View v) {\n" +
            "        int i = v.getId();\n" +
            "        if (i == R.id.action_new_folder) {\n" +
            "            createNewFolder();\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public boolean onLongClick(View v) {\n" +
            "        return false;\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onRefresh() {\n" +
            "        new UpdateList(currentFolder).execute();\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onItemClick(View v, String name, int action) {\n" +
            "        if (action == ACTION_LONG_CLICK) {\n" +
            "            if (name.equals(\"..\")) {\n" +
            "                if (currentFolder.equals(\"/\")) {\n" +
            "                    new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();\n" +
            "                } else {\n" +
            "                    File tempFile = new File(currentFolder);\n" +
            "                    if (tempFile.isFile()) {\n" +
            "                        tempFile = tempFile.getParentFile()\n" +
            "                                .getParentFile();\n" +
            "                    } else {\n" +
            "                        tempFile = tempFile.getParentFile();\n" +
            "                    }\n" +
            "                    new UpdateList(tempFile.getAbsolutePath()).execute();\n" +
            "                }\n" +
            "            } else if (name.equals(getString(R.string.home))) {\n" +
            "                // TODO: 14-Mar-17\n" +
            "                new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();\n" +
            "            }\n" +
            "\n" +
            "//            final File selectedFile = new File(currentFolder, name);\n" +
            "\n" +
            "//            if (selectedFile.isFile() && wantAFile) {\n" +
            "//                // TODO: 15-Mar-17\n" +
            "//                if (listener != null) listener.onFileLongClick(selectedFile);\n" +
            "//            } else if (selectedFile.isDirectory()) {\n" +
            "//            }\n" +
            "        } else if (action == ACTION_CLICK) {\n" +
            "            if (name.equals(\"..\")) {\n" +
            "                if (currentFolder.equals(\"/\")) {\n" +
            "                    new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();\n" +
            "                } else {\n" +
            "                    File tempFile = new File(currentFolder);\n" +
            "                    if (tempFile.isFile()) {\n" +
            "                        tempFile = tempFile.getParentFile()\n" +
            "                                .getParentFile();\n" +
            "                    } else {\n" +
            "                        tempFile = tempFile.getParentFile();\n" +
            "                    }\n" +
            "                    new UpdateList(tempFile.getAbsolutePath()).execute();\n" +
            "                }\n" +
            "                return;\n" +
            "            } else if (name.equals(getString(R.string.home))) {\n" +
            "                // TODO: 14-Mar-17\n" +
            "                new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();\n" +
            "                return;\n" +
            "            }\n" +
            "\n" +
            "            final File selectedFile = new File(currentFolder, name);\n" +
            "//\n" +
            "//            if (selectedFile.isFile() && wantAFile) {\n" +
            "//                // TODO: 15-Mar-17\n" +
            "//                if (listener != null) listener.onFileSelected(selectedFile, request);\n" +
            "//            } else if (selectedFile.isDirectory()) {\n" +
            "            new UpdateList(selectedFile.getAbsolutePath()).execute();\n" +
            "//            }\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    public void refresh() {\n" +
            "        new UpdateList(currentFolder).execute();\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void onRemoveClick(View view, String name, int action) {\n" +
            "        Toast.makeText(activity, \"Don't support this action\", Toast.LENGTH_SHORT).show();\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    private class UpdateList extends AsyncTask<Void, Void, LinkedList<FileDetail>> {\n" +
            "        private String path;\n" +
            "        private String exceptionMessage;\n" +
            "\n" +
            "        public UpdateList(@NonNull String path) {\n" +
            "            this.path = path;\n" +
            "        }\n" +
            "\n" +
            "        @Override\n" +
            "        protected void onPreExecute() {\n" +
            "            super.onPreExecute();\n" +
            "            txtPath.setText(path);\n" +
            "        }\n" +
            "\n" +
            "        @Override\n" +
            "        protected LinkedList<FileDetail> doInBackground(final Void... params) {\n" +
            "            try {\n" +
            "                if (TextUtils.isEmpty(path)) {\n" +
            "                    return null;\n" +
            "                }\n" +
            "\n" +
            "                File tempFolder = new File(path);\n" +
            "                if (tempFolder.isFile()) {\n" +
            "                    tempFolder = tempFolder.getParentFile();\n" +
            "                }\n" +
            "\n" +
            "                String[] canOpen = {\"java\", \"class\", \"jar\"};\n" +
            "\n" +
            "                final LinkedList<FileDetail> fileDetails = new LinkedList<>();\n" +
            "                final LinkedList<FileDetail> folderDetails = new LinkedList<>();\n" +
            "                currentFolder = tempFolder.getAbsolutePath();\n" +
            "\n" +
            "                if (!tempFolder.canRead()) {\n" +
            "\n" +
            "                } else {\n" +
            "                    File[] files = tempFolder.listFiles();\n" +
            "                    for (final File f : files) {\n" +
            "                        if (f.isDirectory() && !f.getName().equalsIgnoreCase(\"fonts\")) {\n" +
            "                            folderDetails.add(new FileDetail(f.getName(), getString(R.string.folder), \"\"));\n" +
            "                        } else if (f.isFile()\n" +
            "                                && FilenameUtils.isExtension(f.getName().toLowerCase(), canOpen)\n" +
            "                                && FileUtils.sizeOf(f) <= Build.MAX_FILE_SIZE * FileUtils.ONE_KB) {\n" +
            "                            final long fileSize = f.length();\n" +
            "                            SimpleDateFormat format = new SimpleDateFormat(\"MMM dd, yyyy  hh:mm a\", Locale.getDefault());\n" +
            "                            String date = format.format(f.lastModified());\n" +
            "                            fileDetails.add(new FileDetail(f.getName(),\n" +
            "\n" +
            "\n" +
            "        @Override\n" +
            "        protected void onPostExecute(final LinkedList<FileDetail> names) {\n" +
            "            if (names != null) {\n" +
            "                boolean isRoot = currentFolder.equals(\"/\");\n" +
            "            }\n" +
            "            super.onPostExecute(names);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "}";

    public void test1() {
        Context context = new Context();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        context.put(DiagnosticListener.class, diagnostics);
        Options.instance(context).put("allowStringFolding", "false");
        JCTree.JCCompilationUnit unit;
        JavacFileManager fileManager = new JavacFileManager(context, true, UTF_8);
        try {
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, ImmutableList.<File>of());
        } catch (IOException e) {
            // impossible
            throw new IOError(e);
        }
        SimpleJavaFileObject source =
                new SimpleJavaFileObject(URI.create("source"), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        return src;
                    }
                };
        Log.instance(context).useSource(source);
        ParserFactory parserFactory = ParserFactory.instance(context);
        Parser parser =
                parserFactory.newParser(
                        src,
            /*keepDocComments=*/ true,
            /*keepEndPos=*/ true,
            /*keepLineMap=*/ true);
        unit = parser.parseCompilationUnit();
        unit.sourcefile = source;
    }

    public void test4() {
        Context context = new Context();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        context.put(DiagnosticListener.class, diagnostics);
        Options.instance(context).put("allowStringFolding", "false");
        JCTree.JCCompilationUnit unit;
        JavacFileManager fileManager = new JavacFileManager(context, true, UTF_8);
        try {
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, ImmutableList.<File>of());
        } catch (IOException e) {
            // impossible
            throw new IOError(e);
        }
        SimpleJavaFileObject source =
                new SimpleJavaFileObject(URI.create("source"), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        return src;
                    }
                };
        Log.instance(context).useSource(source);
        ParserFactory parserFactory = ParserFactory.instance(context);
        Parser parser =
                parserFactory.newParser(
                        src,
            /*keepDocComments=*/ true,
            /*keepEndPos=*/ true,
            /*keepLineMap=*/ true);
        unit = parser.parseCompilationUnit();
        unit.sourcefile = source;
    }

    public void test3() {
        Context context = new Context();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        context.put(DiagnosticListener.class, diagnostics);
        Options.instance(context).put("allowStringFolding", "false");
        JCTree.JCCompilationUnit unit;
        JavacFileManager fileManager = new JavacFileManager(context, true, UTF_8);
        try {
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, ImmutableList.<File>of());
        } catch (IOException e) {
            // impossible
            throw new IOError(e);
        }
        SimpleJavaFileObject source =
                new SimpleJavaFileObject(URI.create("source"), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        return src;
                    }
                };
        Log.instance(context).useSource(source);
        ParserFactory parserFactory = ParserFactory.instance(context);
        Parser parser =
                parserFactory.newParser(
                        src,
            /*keepDocComments=*/ true,
            /*keepEndPos=*/ true,
            /*keepLineMap=*/ true);
        unit = parser.parseCompilationUnit();
        unit.sourcefile = source;
    }

    public void test2() {
        Context context = new Context();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        context.put(DiagnosticListener.class, diagnostics);
        Options.instance(context).put("allowStringFolding", "false");
        JCTree.JCCompilationUnit unit;
        JavacFileManager fileManager = new JavacFileManager(context, true, UTF_8);
        try {
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, ImmutableList.<File>of());
        } catch (IOException e) {
            // impossible
            throw new IOError(e);
        }
        SimpleJavaFileObject source =
                new SimpleJavaFileObject(URI.create("source"), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        return src;
                    }
                };
        Log.instance(context).useSource(source);
        ParserFactory parserFactory = ParserFactory.instance(context);
        Parser parser =
                parserFactory.newParser(
                        src,
            /*keepDocComments=*/ true,
            /*keepEndPos=*/ true,
            /*keepLineMap=*/ true);
        unit = parser.parseCompilationUnit();
        unit.sourcefile = source;
    }
}