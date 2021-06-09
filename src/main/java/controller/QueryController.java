package controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fxmisc.richtext.*;
import org.fxmisc.richtext.model.*;
import utils.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class QueryController {
    @FXML
    private Button theBtExecute;
    @FXML
    private Button theBtPrevPage;
    @FXML
    private Button theBtNextPage;
    @FXML
    private QueryModel theModel;
    @FXML
    private ComboBox<String> theComboRowCount;
    @FXML
    private TextField theEditPageNum;
    @FXML
    private Label theLabelTotalPages;
    @FXML
    private ComboBox<String> theComboConns;
    @FXML
    private InlineCssTextArea theTextCommands;
    @FXML
    private TableView<ObservableList<String>> theTableResult;

    private ObservableList<ObservableList<String>> theResultData;
    // variable for pagination
    private int theTotalPages;
    private int theRowsPerPage;
    private int theCurPageNum;

    /**
     * constructor
     */
    public QueryController() {
        theTotalPages = 1;
        theRowsPerPage = 1;
        theCurPageNum = 1;
    }

    /**
     * initialize controller
     * @param viewName
     */
    public void initialization(String viewName) {
        theTextCommands.setParagraphGraphicFactory(LineNumberFactory.get(theTextCommands));
        theTextCommands.setWrapText(true);
        theTextCommands.caretPositionProperty().addListener(((observable, oldValue, newValue) -> {
            TwoDimensional.Position oldPos = (TwoDimensional.Position) theTextCommands.offsetToPosition(oldValue, TwoDimensional.Bias.Forward);
            TwoDimensional.Position newPos = (TwoDimensional.Position) theTextCommands.offsetToPosition(newValue, TwoDimensional.Bias.Forward);
            theTextCommands.setStyle(oldPos.getMajor(), "-rtfx-background-color: white;");
            theTextCommands.setStyle(newPos.getMajor(), "-rtfx-background-color: pink;");
        }));

        String[] connNames = ConnInfoList.getInstance().getConnNames();
        theComboConns.getItems().addAll(connNames);

        String queries = theModel.getQueries(viewName);
        String connName = theModel.getSelectedConnName(viewName);

        if(connName != null)
            theComboConns.getSelectionModel().select(connName);
        else
            theComboConns.getSelectionModel().selectFirst();
        if(queries != null)
            theTextCommands.replaceText(queries);
        // initialize row count combobox
        String[] rowCounts = {"10", "20", "50", "100", "All"};
        theComboRowCount.getItems().addAll(rowCounts);
        theComboRowCount.getSelectionModel().selectFirst();

        theBtPrevPage.setPrefSize(25, 25);
        theBtNextPage.setPrefSize(25, 25);
    }

    public void updateConnList() {
        theComboConns.getItems().clear();
        String[] connNames = ConnInfoList.getInstance().getConnNames();
        theComboConns.getItems().addAll(connNames);
        theComboConns.getSelectionModel().selectFirst();
    }
    /**
     * set model to controller
     * @param theModel
     */
    public void setModel(QueryModel theModel) {
        this.theModel =  theModel;
    }

    /**
     * remove selected view status
     * @param viewName
     */
    public void removeStatus(String viewName) {
        theModel.removeStatus(viewName);
    }

    /**
     * save selected view status, only in memory not in file
     * @param windowName
     */
    public void saveStatus(String windowName) {
        String connName = theComboConns.getSelectionModel().getSelectedItem();
        String queries = theTextCommands.getText();

        theModel.saveStatus(windowName, connName, queries);
    }

    /**
     * execute query
     * @param actionEvent
     */
    public void onExecute(ActionEvent actionEvent) {
        execute();
    }

    /**
     * execute query
     */
    public void execute() {
        int currentParagraph = theTextCommands.getCurrentParagraph();
        //String query = theTextCommands.getParagraph(currentParagraph).getSegments().get(0);
        String query = theTextCommands.getSelectedText();
        if(query == null)
            return;

        String connName = theComboConns.getSelectionModel().getSelectedItem();
        if(!ConnInfoList.getInstance().isValid(connName)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ticket for connection is timed out.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        ConnInfo info = ConnInfoList.getInstance().getConnInfo(connName);
        System.out.println("Calling SOAP");
        String result = SoapAPI.callQueryService(info.getServiceUrl(), "runReportInSession", query, info.getSessionId());

        String [] resultList = result.split("\n");
        theTableResult.getItems().clear();
        theTableResult.getColumns().clear();
        // add headers
        String [] columns = resultList[0].split(",");
        for(int i=0; i<columns.length; i++) {
            final int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columns[i].trim());
            column.setCellValueFactory(
                    param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
            theTableResult.getColumns().add(column);
        }

        parseResult(result);
        calculatePages();
        updatePage();
        //TableViewSelectionModel selectionModel = theTableResult.getSelectionModel();
        //selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        theTableResult.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
           @Override
           public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
   //Check whether item is selected and set value of selected item to Label
   if(theTableResult.getSelectionModel().getSelectedItem() != null) 
   {    
     TableViewSelectionModel selectionModel = theTableResult.getSelectionModel();
     selectionModel.setCellSelectionEnabled(true);
     ObservableList selectedCells = selectionModel.getSelectedCells();
     TablePosition tablePosition = (TablePosition) selectedCells.get(0);
     Object val = tablePosition.getTableColumn().getCellData(newValue);
     Clipboard clipboard = Clipboard.getSystemClipboard();
     ClipboardContent content = new ClipboardContent();
     content.putString(val.toString());
     clipboard.setContent(content);
     //System.out.println("Selected Value" + val);
    }
    }
        });
    }

    /**
     * parse response string to list
     * @param result String
     */
    private void parseResult(String result) {
        String [] resultList = result.split("\n");
        theResultData = FXCollections.observableArrayList();
        // add rows
        for(int i=1; i<resultList.length; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.clear();
            String [] items = resultList[i].split(",");
            for (String item : items) {
                row.add(item.trim());
            }
            theResultData.add(row);
        }
    }

    /**
     * export result to xls/csv
     * @param actionEvent event
     */
    public void onExport(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        // set filter
        List<FileChooser.ExtensionFilter> filters = new ArrayList<>();
        filters.add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
        filters.add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.getExtensionFilters().addAll(filters);
        // show save file dialog
        File selectedFile = fileChooser.showSaveDialog(null);
        if(selectedFile == null)
            return;
        // get file path
        String filePath = selectedFile.getAbsolutePath();

        String [] pathTokens = filePath.split("\\.");
        String ext = pathTokens[pathTokens.length-1];
        try {
            if (ext.equals("xlsx")) {
                exportToXlsx(filePath);
            } else {
                exportToCSV(filePath);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Export Success.", ButtonType.OK);
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "onExport"+e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * internal export to excel
     * @param filePath file path
     * @throws IOException io exception
     */
    private void exportToXlsx(String filePath) throws IOException {
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook();

        String newSheetName = "Sheet_1";
        Sheet sheet = workbook.createSheet(newSheetName);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        List<TableColumn> columnList = new ArrayList<>(theTableResult.getColumns());
        String[] columns = columnList.stream().map(TableColumnBase::getText).toArray(String[]::new);

        // Creating cells of header row
        for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }
        // Create Other rows and cells with employees data
        int rowNum = 1;
        for(ObservableList<String> rowData : theResultData) {
            int cellIdx = 0;
            Row row = sheet.createRow(rowNum++);
            for(String cell : rowData) {
                row.createCell(cellIdx++).setCellValue(cell);
            }
        }

        // Resize all columns to fit the content size
        for(int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();

        workbook.close();
    }

    /**
     * internal export to csv
     * @param filePath file path
     * @throws IOException io exception
     */
    private void exportToCSV(String filePath) throws IOException {
        List<TableColumn> columns = new ArrayList<>(theTableResult.getColumns());
        String columnNames = columns.stream().map(TableColumnBase::getText).collect(Collectors.joining(","));
        columnNames += "\n";
        StringBuilder rowValues = new StringBuilder();
        for(ObservableList<String> row : theResultData) {
            rowValues.append(String.join(",", row)).append("\n");
        }

        String result = columnNames + rowValues;

        FileOutputStream fileOut = new FileOutputStream(filePath);
        byte[] bytes = result.getBytes();
        fileOut.write(bytes, 0, bytes.length);
        fileOut.close();
    }

    /**
     * change connection event handler
     * @param actionEvent event
     */
    public void onChangeConns(ActionEvent actionEvent) {
        String connName = theComboConns.getSelectionModel().getSelectedItem();
        ConnInfo info = ConnInfoList.getInstance().getConnInfo(connName);
        String sessionId = SoapAPI.callLoginService(info.getServiceUrl(), info.getServiceOperation(), info.getUserName(), info.getPassword());
        if(sessionId.isEmpty())
            return;
        theModel.saveConnection(
                info.getConnectionName(),
                info.getUserName(),
                info.getPassword(),
                info.getServiceUrl(),
                info.getServiceOperation(),
                sessionId,
                LocalDateTime.now()
        );
    }

    /**
     * pagination prev button event handler
     * @param actionEvent event
     */
    public void onPrevPage(ActionEvent actionEvent) {
        if(theCurPageNum == 1)
            return;
        theCurPageNum--;
        theEditPageNum.setText(String.valueOf(theCurPageNum));
        updatePage();
    }

    /**
     * rows per page change event handler
     * @param actionEvent event
     */
    public void onChangeRowCount(ActionEvent actionEvent) {
        calculatePages();
        updatePage();
    }

    /**
     * page number edit event handler
     * @param actionEvent event
     */
    public void onChangePageNumber(ActionEvent actionEvent) {
    }

    /**
     * pagination next button event handler
     * @param actionEvent event
     */
    public void onNextPage(ActionEvent actionEvent) {
        if(theCurPageNum == theTotalPages)
            return;
        theCurPageNum++;
        theEditPageNum.setText(String.valueOf(theCurPageNum));
        updatePage();
    }

    /**
     * internal update page
     */
    private void updatePage() {
        if(theResultData == null || theResultData.isEmpty())
            return;

        int startIdx = (theCurPageNum - 1) * theRowsPerPage;
        int endIdx;
        if(theCurPageNum != theTotalPages)
            endIdx = theCurPageNum * theRowsPerPage;
        else
            endIdx = theResultData.size();

        ObservableList<ObservableList<String>> pageData = FXCollections.observableArrayList(theResultData.subList(startIdx, endIdx));
        theTableResult.getItems().clear();
        // add rows
        for(int i=1; i<pageData.size(); i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.clear();
            String [] items = pageData.get(i).toArray(new String[0]);
            Collections.addAll(row, items);
        }
        theTableResult.setItems(pageData);
    }

    /**
     * internal calculate pages
     */
    private void calculatePages() {
        if(theResultData == null || theResultData.isEmpty())
            return;

        int totalRows = theResultData.size();
        String rows = theComboRowCount.getSelectionModel().getSelectedItem();
        if(rows.equals("All")) {
            theTotalPages = 1;
            theRowsPerPage = totalRows;
        } else {
            theRowsPerPage = Integer.parseInt(rows);
            theTotalPages = (int) Math.ceil((float)totalRows / (float)theRowsPerPage);
        }
        theEditPageNum.setText("1");
        theCurPageNum = 1;
        theLabelTotalPages.setText("/" + theTotalPages);
    }
}