package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.MappingService;
import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;
import io.github.cadiboo.optifinedeobf.mapping.TSRG2MCP;
import io.github.cadiboo.optifinedeobf.util.CustomFileFilter;
import io.github.cadiboo.optifinedeobf.util.FixedJarInputStream;
import io.github.cadiboo.optifinedeobf.util.Utils;
import org.objectweb.asm.ClassReader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class DeobfFrame extends JFrame {

	public static final FileFilter JAVA_FILE_FILTER = new CustomFileFilter("Jar and class files", f -> f.getName().endsWith(".class") || f.getName().endsWith(".jar"));
	public static final FileFilter MAPPINGS_FILE_FILTER = new CustomFileFilter("Mappings files", f -> f.getName().endsWith(".srg") || f.getName().endsWith(".tsrg"));

	private static final int SRG_SLASH_LENGTH = "srg/".length();

	private final JTextField inputFileTextField;
	private final JTextField outputFileTextField;
	private final JProgressBar progressBar;
	private final JLabel progressTitle;
	private final JLabel progressSubTitle;
	private final JCheckBox makePublicCheckbox;
	private final JCheckBox definaliseCheckbox;
	private final JCheckBox remapFileNamesCheckbox;
	private final JCheckBox makeForgeDevJarCheckbox;
	private final JTextField mappingsFileTextField;
	private ClassRemapper classRemapper = null;
	private MappingService mappingService = null;

	private DeobfFrame() throws HeadlessException {
		this.setName("DeobfFrame");
		this.setSize(400, 300);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setTitle("OptiFine Deobfuscator");

		var title = new JLabel();
		title.setName("title");
		title.setBounds(2, 5, 385, 42);
		title.setFont(new Font("Dialog", Font.BOLD, 18));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setPreferredSize(new Dimension(385, 42));
		title.setText("OptiFine Deobfuscator");

		var description = new JLabel();
		description.setName("description");
		description.setBounds(2, 30, 385, 42);
		description.setFont(new Font("Dialog", Font.PLAIN, 12));
		description.setHorizontalAlignment(SwingConstants.CENTER);
		description.setPreferredSize(new Dimension(385, 42));
		description.setText("This deobfuscator will deobfuscate (extracted) OptiFine");

		var inputFileLabel = new JLabel();
		inputFileLabel.setName("inputFileLabel");
		inputFileLabel.setBounds(15, 75, 60, 16);
		inputFileLabel.setPreferredSize(new Dimension(75, 16));
		inputFileLabel.setText("Input File");

		var inputDropTarget = new DropTarget() {
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					for (File file : (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
						setInputFile(file);
					}
					evt.dropComplete(true);
				} catch (Exception e) {
					handleException(e);
				}
			}
		};

		inputFileTextField = new JTextField();
		inputFileTextField.setName("inputFileTextField");
		inputFileTextField.setBounds(90, 75, 210, 20);
		inputFileTextField.setEditable(false);
		inputFileTextField.setPreferredSize(new Dimension(210, 20));
		inputFileTextField.setDropTarget(inputDropTarget);

		var inputFileButton = new JButton();
		inputFileButton.setName("inputFileButton");
		inputFileButton.setBounds(300, 75, 75, 20);
		inputFileButton.setMargin(new Insets(2, 2, 2, 2));
		inputFileButton.setPreferredSize(new Dimension(75, 20));
		inputFileButton.setText("Choose...");
		inputFileButton.addActionListener(e -> this.chooseInputFile());
		inputFileButton.setDropTarget(inputDropTarget);

		var outputFileLabel = new JLabel();
		outputFileLabel.setName("outputFileLabel");
		outputFileLabel.setBounds(15, 100, 75, 16);
		outputFileLabel.setPreferredSize(new Dimension(75, 16));
		outputFileLabel.setText("Output File");

		outputFileTextField = new JTextField();
		outputFileTextField.setName("outputFileTextField");
		outputFileTextField.setBounds(90, 100, 210, 20);
		outputFileTextField.setEditable(true);
		outputFileTextField.setPreferredSize(new Dimension(210, 20));

		var outputFileButton = new JButton();
		outputFileButton.setName("outputFileButton");
		outputFileButton.setBounds(300, 100, 75, 20);
		outputFileButton.setMargin(new Insets(2, 2, 2, 2));
		outputFileButton.setPreferredSize(new Dimension(75, 20));
		outputFileButton.setText("Choose...");
		outputFileButton.addActionListener(e -> this.chooseOutputFile());

		var mappingsDropTarget = new DropTarget() {
			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					for (File file : (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
						setMappings(file);
					}
					evt.dropComplete(true);
				} catch (Exception e) {
					handleException(e);
				}
			}
		};

		var mappingsFileLabel = new JLabel();
		mappingsFileLabel.setName("mappingsFileLabel");
		mappingsFileLabel.setBounds(15, 125, 75, 16);
		mappingsFileLabel.setPreferredSize(new Dimension(75, 16));
		mappingsFileLabel.setText("Mappings");

		mappingsFileTextField = new JTextField("Choose Mappings...");
		mappingsFileTextField.setName("mappingsFileTextField");
		mappingsFileTextField.setBounds(90, 125, 210, 20);
		mappingsFileTextField.setEditable(false);
		mappingsFileTextField.setPreferredSize(new Dimension(210, 20));
		mappingsFileTextField.setDropTarget(mappingsDropTarget);

		var mappingsFileButton = new JButton();
		mappingsFileButton.setName("mappingsFileButton");
		mappingsFileButton.setBounds(300, 125, 75, 20);
		mappingsFileButton.setMargin(new Insets(2, 2, 2, 2));
		mappingsFileButton.setPreferredSize(new Dimension(75, 20));
		mappingsFileButton.setText("Choose...");
		mappingsFileButton.addActionListener(e -> this.chooseMappingsFile());
		mappingsFileButton.setDropTarget(mappingsDropTarget);

		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(1);
		progressBar.setValue(1);
		progressBar.setBounds(15, 150, 360, 10);
		progressBar.setVisible(false);

		progressTitle = new JLabel();
		progressTitle.setName("progressTitle");
		progressTitle.setBounds(15, 160, 360, 15);
		progressTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
		progressTitle.setHorizontalAlignment(SwingConstants.CENTER);
		progressTitle.setPreferredSize(new Dimension(360, 15));

		progressSubTitle = new JLabel();
		progressSubTitle.setName("progressSubTitle");
		progressSubTitle.setBounds(15, 175, 360, 15);
		progressSubTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
		progressSubTitle.setHorizontalAlignment(SwingConstants.CENTER);
		progressSubTitle.setPreferredSize(new Dimension(360, 15));

		makePublicCheckbox = new JCheckBox();
		makePublicCheckbox.setName("makePublicCheckbox");
		makePublicCheckbox.setBounds(0, 0, 200, 20);
		makePublicCheckbox.setText("Make Public");
		makePublicCheckbox.setToolTipText("If classes, fields and methods should have their access escalated to public. Similar to an access transformer");

		definaliseCheckbox = new JCheckBox();
		definaliseCheckbox.setName("definaliseCheckbox");
		definaliseCheckbox.setBounds(0, 0, 200, 20);
		definaliseCheckbox.setText("Definalise");
		definaliseCheckbox.setToolTipText("If classes, fields and methods should have their access definalised. Similar to an access transformer");

		remapFileNamesCheckbox = new JCheckBox();
		remapFileNamesCheckbox.setName("remapFileNamesCheckbox");
		remapFileNamesCheckbox.setBounds(0, 0, 200, 20);
		remapFileNamesCheckbox.setText("Remap file names");
		remapFileNamesCheckbox.setToolTipText("If class files should be saved under their remapped name");

		makeForgeDevJarCheckbox = new JCheckBox();
		makeForgeDevJarCheckbox.setName("makeForgeDevJarCheckbox");
		makeForgeDevJarCheckbox.setBounds(0, 0, 200, 20);
		makeForgeDevJarCheckbox.setText("Forge Dev Jar");
		makeForgeDevJarCheckbox.setToolTipText("If OptiFine should have some tweaks applied to make it a valid for use in a Forge mod development workspace");
		makeForgeDevJarCheckbox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				remapFileNamesCheckbox.setSelected(false);
				remapFileNamesCheckbox.setEnabled(false);
			} else {
				remapFileNamesCheckbox.setEnabled(true);
			}
		});

		var deobfButton = new JButton();
		deobfButton.setName("deobfButton");
		deobfButton.setBounds(350, 10, 0, 0);
		deobfButton.setMargin(new Insets(2, 2, 2, 2));
		deobfButton.setPreferredSize(new Dimension(50, 20));
		deobfButton.setText("Deobf");
		deobfButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				deobfButton.setEnabled(false);
				DeobfFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				progressBar.setVisible(true);
				try {
					deobf();
					progressBar.setVisible(false);
				} finally {
					DeobfFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					deobfButton.setEnabled(true);
				}
				return null;
			}
		}.execute());

		var centerPanel = new JPanel();
		centerPanel.setName("centerPanel");
		centerPanel.setLayout(null);

		var bottomPanel = new JPanel();
		bottomPanel.setName("bottomPanel");
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
		bottomPanel.setPreferredSize(new Dimension(400, 100));

		var contentPanel = new JPanel();
		contentPanel.setName("contentPanel");
		contentPanel.setLayout(new BorderLayout(5, 5));
		contentPanel.setPreferredSize(new Dimension(400, 300));

		{
			centerPanel.add(title, title.getName());
			centerPanel.add(description, description.getName());
			centerPanel.add(inputFileLabel, inputFileLabel.getName());
			centerPanel.add(inputFileTextField, inputFileTextField.getName());
			centerPanel.add(inputFileButton, inputFileButton.getName());
			centerPanel.add(outputFileLabel, outputFileLabel.getName());
			centerPanel.add(outputFileTextField, outputFileTextField.getName());
			centerPanel.add(outputFileButton, outputFileButton.getName());
			centerPanel.add(mappingsFileLabel, mappingsFileLabel.getName());
			centerPanel.add(mappingsFileTextField, mappingsFileTextField.getName());
			centerPanel.add(mappingsFileButton, mappingsFileButton.getName());
			centerPanel.add(progressBar, progressBar.getName());
			centerPanel.add(progressTitle, progressTitle.getName());
			centerPanel.add(progressSubTitle, progressSubTitle.getName());

			bottomPanel.add(makePublicCheckbox, makePublicCheckbox.getName());
			bottomPanel.add(definaliseCheckbox, definaliseCheckbox.getName());
			bottomPanel.add(remapFileNamesCheckbox, remapFileNamesCheckbox.getName());
			bottomPanel.add(makeForgeDevJarCheckbox, makeForgeDevJarCheckbox.getName());
			bottomPanel.add(deobfButton, deobfButton.getName());

			contentPanel.add(centerPanel, "Center");
			contentPanel.add(bottomPanel, "South");
		}

		this.setContentPane(contentPanel);
		this.pack();
	}

	public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		DeobfFrame frame = new DeobfFrame();
		// Centre Window
		{
			var rect = frame.getBounds();
			var scrDim = Toolkit.getDefaultToolkit().getScreenSize();
			var parRect = new Rectangle(0, 0, scrDim.width, scrDim.height);
			int newX = parRect.x + (parRect.width - rect.width) / 2;
			int newY = parRect.y + (parRect.height - rect.height) / 2;
			if (newX < 0)
				newX = 0;
			if (newY < 0)
				newY = 0;
			frame.setBounds(newX, newY, rect.width, rect.height);
		}
		frame.setVisible(true);
	}

	private void handleException(Exception e) {
		e.printStackTrace();
		setProgressText("Error: " + e.getLocalizedMessage(), e.toString());
	}

	private void setProgressText(String title, String subTitle) {
		progressTitle.setText(title);
		progressSubTitle.setText(subTitle);
	}

	private void deobf() {
		var inputPath = Paths.get(inputFileTextField.getText());
		var inputFile = inputPath.toFile();
		if (!inputFile.exists()) {
			setProgressText("Please select an input file.", "Input file does not exist.");
			return;
		}

		var outputPath = Paths.get(outputFileTextField.getText());
		var outputFile = outputPath.toFile();
		if (outputFile.exists() && !outputFile.delete()) {
			setProgressText("Failed to delete pre-existing output file.", "Either delete it manually or choose a different output path.");
			return;
		}

		if (mappingService == null)
			createMappingsService();
		if (classRemapper == null || (classRemapper.mappingService != mappingService || classRemapper.makePublic != makePublicCheckbox.isSelected() || classRemapper.definalise != definaliseCheckbox.isSelected()))
			classRemapper = new ClassRemapper(mappingService, makePublicCheckbox.isSelected(), definaliseCheckbox.isSelected());

		var inputFileName = inputFile.getName();
		setProgressText("Remapping file " + inputFileName + ".", "");

		try {
			if (inputFileName.endsWith(".jar"))
				remapJar(inputFile, outputFile);
			else if (inputFileName.endsWith(".class"))
				remapClass(inputPath, outputPath);
			else
				setProgressText("Please select a valid input file.", "Input file is not a jar or class file.");
			setProgressText("Remapped file " + inputFileName + ".", "Output: " + outputFile.getName() + ".");
		} catch (Exception e) {
			handleException(e);
		}
	}

	private void remapClass(Path inputPath, Path outputPath) throws IOException {
		byte[] remappedBytes = classRemapper.remapClass(Files.readAllBytes(inputPath));
		if (remappedBytes != null)
			Files.write(outputPath, remappedBytes);
	}

	private void remapJar(File inputFile, File outputFile) throws IOException {
		var makeForgeDevJar = makeForgeDevJarCheckbox.isSelected();
		var remapFileNames = remapFileNamesCheckbox.isSelected();

		var filesToIngore = new HashSet<String>();
		// Add one because we also have the "writing jar" step
		progressBar.setMaximum(getFilesToProcess(inputFile, filesToIngore) + 1);
		progressBar.setValue(0);

		try (
				var jarInputStream = new FixedJarInputStream(inputFile, false);
				var jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile))
		) {
			if (makeForgeDevJar) {
				progressBar.setMaximum(progressBar.getMaximum() + 1);
				progressBar.setValue(progressBar.getValue() + 1);
				var progressText = "Injecting dummy OptiFine mod class.";
				progressSubTitle.setText(progressText);
				progressBar.setToolTipText(progressText);

				// Inject dummy OptiFine mod class to stop loading errors in dev
				jarOutputStream.putNextEntry(new JarEntry("optifine/OptiFineDeobfInjectedOptiFineModClass.class"));
				jarOutputStream.write(Utils.readStreamFully(getClass().getResourceAsStream("/OptiFineDeobfInjectedOptiFineModClass.class")));
				jarOutputStream.closeEntry();
			}
			JarEntry jarEntry;
			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				var jarEntryName = jarEntry.getName();

				progressBar.setValue(progressBar.getValue() + 1);
				var progressText = "Processing " + jarEntryName + ".";
				progressSubTitle.setText(progressText);
				progressBar.setToolTipText(progressText);

				if (filesToIngore.contains(jarEntryName))
					continue;

				if (jarEntryName.endsWith(".class")) {
					byte[] remappedBytes;
					try {
						remappedBytes = classRemapper.remapClass(Utils.readStreamFully(jarInputStream));
					} catch (Exception e) {
						handleException(e);
						throw e;
					}
					if (remappedBytes != null) {
						if (remapFileNames)
							jarOutputStream.putNextEntry(new JarEntry(new ClassReader(remappedBytes).getClassName() + ".class"));
						else
							jarOutputStream.putNextEntry(new JarEntry(jarEntryName));
						jarOutputStream.write(remappedBytes);
						jarOutputStream.closeEntry();

						if (makeForgeDevJar && jarEntryName.startsWith("srg/")) {
							// Duplicate srg named classes and overwrite their obf named counterparts
							jarOutputStream.putNextEntry(new JarEntry(jarEntryName.substring(SRG_SLASH_LENGTH)));
							jarOutputStream.write(remappedBytes);
							jarOutputStream.closeEntry();
						}
					}
				} else {
					jarOutputStream.putNextEntry(new JarEntry(jarEntry));
					jarOutputStream.write(Utils.readStreamFully(jarInputStream));
					jarOutputStream.closeEntry();
				}
			}

			progressBar.setValue(progressBar.getValue() + 1);
			var progressText = "Writing " + outputFile.getName() + ".";
			progressSubTitle.setText(progressText);
			progressBar.setToolTipText(progressText);
			jarOutputStream.flush();
		}

		progressBar.setMaximum(1);
		progressBar.setValue(1);
		progressBar.setToolTipText(null);
		progressSubTitle.setText("");
	}

	private int getFilesToProcess(File inputFile, HashSet<String> filesToIgnore) throws IOException {
		var makeForgeDevJar = makeForgeDevJarCheckbox.isSelected();
		var remapFileNames = remapFileNamesCheckbox.isSelected();

		var progressText = "Gathering files to process.";
		progressSubTitle.setText(progressText);
		progressBar.setToolTipText(progressText);

		int filesToProcess = 0;
		try (var jarInputStream = new FixedJarInputStream(inputFile, false)) {
			JarEntry jarEntry;
			while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
				++filesToProcess;
				String jarEntryName = jarEntry.getName();
				if (makeForgeDevJar && jarEntryName.startsWith("net/minecraftforge/") // Discard Forge dummy classes
						|| jarEntryName.startsWith("javax/") // Discard Javax dummy class
						|| (!jarEntryName.equals("Config.class") && jarEntryName.endsWith(".class") && !jarEntryName.contains("/")) // Discard obf named classes
				) {
					filesToIgnore.add(jarEntryName);
				}
				if ((makeForgeDevJar || remapFileNames) && jarEntryName.startsWith("srg/"))
					// Mark obf-named classes with SRG counterparts as ignored so we don't try and add them to the zip twice
					filesToIgnore.add(jarEntryName.substring(SRG_SLASH_LENGTH));
			}
		}
		return filesToProcess;
	}

	private void createMappingsService() {
		var mappingsText = mappingsFileTextField.getText();
		var file = Paths.get(mappingsText).toFile();
		if (file.exists())
			setMappings(file);
		else
			handleException(new IllegalStateException("Invalid mappings input"));
	}

	private JFileChooser setFileFilter(JFileChooser chooser, FileFilter filter) {
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(filter);
		return chooser;
	}

	private void chooseInputFile() {
		var chooser = setFileFilter(new JFileChooser(), JAVA_FILE_FILTER);
		chooser.setDialogTitle("Select jar to deobfuscate");
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			setInputFile(chooser.getSelectedFile());
	}

	private void chooseMappingsFile() {
		File startDirectory = null;
		int result = JOptionPane.showConfirmDialog(null, "Would you like to select a project folder to search for mappings files?\nClick No to select a mappings file manually.", "Select project folder", JOptionPane.YES_NO_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
			return;
		if (result == JOptionPane.YES_OPTION) {
			var chooser = new JFileChooser();
			chooser.setDialogTitle("Select project folder");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			var mappingsFolder = Paths.get(chooser.getSelectedFile().getPath(), "build", "fg_cache", "de", "oceanlabs", "mcp", "mcp_config").toFile();
			if (mappingsFolder.isDirectory())
				startDirectory = mappingsFolder;
		}
		var chooser = setFileFilter(new JFileChooser(startDirectory), MAPPINGS_FILE_FILTER);
		chooser.setDialogTitle("Select mappings file");
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			setMappings(chooser.getSelectedFile());
	}

	private void chooseOutputFile() {
		var chooser = setFileFilter(new JFileChooser(), JAVA_FILE_FILTER);
		chooser.setDialogTitle("Save deobfuscated jar");
		if (!"".equals(outputFileTextField.getText()))
			chooser.setSelectedFile(new File(outputFileTextField.getText()));
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			outputFileTextField.setText(chooser.getSelectedFile().getPath());
	}

	private void setInputFile(File file) {
		var fileName = file.getName();
		if (fileName.endsWith(".jar")) {
			outputFileTextField.setText(file.getParent() + File.separator + Utils.replaceLast(fileName, ".jar", "-deobf.jar"));

			makeForgeDevJarCheckbox.setEnabled(true);
			remapFileNamesCheckbox.setEnabled(true);
		} else if (fileName.endsWith(".class")) {
			outputFileTextField.setText(file.getParent() + File.separator + Utils.replaceLast(fileName, ".class", "-deobf.class"));

			makeForgeDevJarCheckbox.setSelected(false);
			makeForgeDevJarCheckbox.setEnabled(false);
			remapFileNamesCheckbox.setSelected(false);
			remapFileNamesCheckbox.setEnabled(false);
		} else {
			handleException(new IllegalStateException("Input file is not a jar or class file! Please select a valid input file."));
			return;
		}
		inputFileTextField.setText(file.getPath());
	}

	private void setMappings(File file) {
		var fileName = file.getName();
		if (fileName.endsWith(".srg")) {
			try {
				mappingService = new SRG2MCP(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				handleException(e);
				return;
			}
		} else if (fileName.endsWith(".tsrg"))
			try {
				mappingService = new TSRG2MCP(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				handleException(e);
				return;
			}
		else {
			handleException(new IllegalStateException("Mappings file is not a srg or tsrg file! Please select a valid mappings file."));
			return;
		}
		mappingsFileTextField.setText(file.getPath());
	}

}
