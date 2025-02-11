package org.bimrocket.ihub.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.python.jline.internal.Log;
import org.apache.commons.compress.utils.Lists;

/**
 * 
 * @author wilberquito
 */
public class ExcelMapper
{
    /**
     * returns and empty list when there was a problem
     * 
     * @param pathf
     * @param hasHeader
     * @return
     */
    public static List<Map<String, String>> mapping(File file,
            ExcelEnum excelEnum, boolean hasHeader) throws Exception
    {
        List<Map<String, String>> result = new ArrayList<>();
        try
        {
            Workbook book = null;

            if (excelEnum == ExcelEnum.XLSX)
            {
                book = new XSSFWorkbook(file);
            }
            else if (excelEnum == ExcelEnum.XLS)
            {
                book = new HSSFWorkbook(new FileInputStream(file));
            }
            else
            {
                String err = String.format(
                        "@mapping: unsuported extension - '{}'", excelEnum);
                throw new Exception(err);
            }

            Sheet sheet = book.getSheetAt(0);
            List<Row> rows = Lists.newArrayList(sheet.iterator());

            if (hasHeader && rows.size() < 1)
            {
                book.close();
                throw new Exception(
                        "@mapping: can not generate header without excel data");
            }

            List<String> headers = hasHeader ? obteinHeaders(sheet)
                    : buildHeaders(sheet);

            int size = headers.size();
            List<Row> data = rows.subList(hasHeader ? 1 : 0, rows.size());

            for (Row row : data)
            {
                Map<String, String> dict = new HashMap<>();
                for (int i = 0; i < size; i++)
                {
                    String header = headers.get(i);
                    Cell cell = row.getCell(i);
                    dict.put(header, cellParser(cell));
                }
                if (!Functions.nullableMap(dict))
                {
                    result.add(cleanMap(dict));
                }
            }
            book.close();
        }
        catch (Exception e)
        {
            Log.error(
                    "@mapping: problem mapping file content to data structure. Error: \n",
                    e.getMessage());
            throw e;
        }
        return result;
    }

    /**
     * builds syntetic headers for mapping relation
     * 
     * @param sheet
     * @return
     */
    static List<String> buildHeaders(Sheet sheet)
    {
        List<String> headers = new ArrayList<>();
        int rowIdx = sheet.getFirstRowNum();
        if (rowIdx < 0)
        {
            Log.info("@buildHeaders: expected to have first row");
        }
        else
        {
            Row row = sheet.getRow(0);
            int colIdx = row.getLastCellNum();

            for (int i = 0; i < colIdx; i++)
            {
                headers.add(String.format("c%d", i + 1));
            }
        }
        return headers;
    }

    static List<String> obteinHeaders(Sheet sheet)
    {
        List<String> headers = new ArrayList<>();
        int rowIdx = sheet.getFirstRowNum();
        if (rowIdx < 0)
        {
            Log.info("@obteinHeaders: expected to have first row");
        }
        else
        {
            Row row = sheet.getRow(0);
            for (Cell cell : row)
            {
                headers.add(cell.getStringCellValue());
            }
        }
        return headers;
    }

    static String cellParser(Cell cell)
    {
        if (cell == null)
            return null;

        switch (cell.getCellType())
        {
        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell))
            {
                return String.valueOf(cell.getDateCellValue());
            }
            else
            {
                return String.valueOf(cell.getNumericCellValue());
            }
        case STRING:
            return String.valueOf(cell.getStringCellValue());
        default:
            return null;
        }
    }

    /**
     * mutates input state
     * 
     * @param dict
     * @return
     */
    static <T> Map<String, T> cleanMap(Map<String, T> dict)
    {
        if (dict == null)
            return null;

        if (dict.containsKey(""))
        {
            dict.remove("");
        }
        return dict;
    }
}
