package org.bimrocket.ihub.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;

import org.apache.commons.io.FilenameUtils;
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
public class ExcelMapping
{
    /**
     * returns and empty list when there was a problem
     * 
     * @param pathf
     * @param hasHeader
     * @return
     */
    public List<Map<String, String>> mapping(String pathf, boolean hasHeader)
    {
        List<Map<String, String>> result = new ArrayList<>();
        try
        {
            String extension = FilenameUtils.getExtension(pathf);
            Workbook book = null;

            if (extension.equalsIgnoreCase("xlsx"))
            {
                File fis = new File(pathf);
                book = new XSSFWorkbook(fis);
            }
            else if (extension.equalsIgnoreCase("xls"))
            {
                FileInputStream fis = new FileInputStream(pathf);
                book = new HSSFWorkbook(fis);
            }
            else
            {
                String err = String.format(
                        "@mapping: unsuported extension - '{}'", extension);
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
            List<Row> data = rows.subList(1, rows.size());

            for (Row row : data)
            {
                Map<String, String> dict = new HashMap<>();
                for (int i = 0; i < size; i++)
                {
                    String header = headers.get(i);
                    Cell cell = row.getCell(i);
                    dict.put(header, cellParser(cell));
                }
                CollectionUtils.filter(dict.values(),
                        PredicateUtils.notNullPredicate());
            }
            book.close();
        }
        catch (Exception e)
        {
            Log.error(
                    "@mapping: problem mapping file content to data structure. Error: \n",
                    e.getMessage());
        }
        return result;
    }

    List<String> buildHeaders(Sheet sheet)
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
                headers.add(String.format("c{}", i + 1));
            }
        }
        return headers;
    }

    List<String> obteinHeaders(Sheet sheet)
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

    String cellParser(Cell cell)
    {
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
}
