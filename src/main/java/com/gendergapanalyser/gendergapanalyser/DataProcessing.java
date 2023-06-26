package com.gendergapanalyser.gendergapanalyser;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.List;
import java.util.*;

public class DataProcessing {
    //Locating the resources folder where the datasets should be placed
    private final File folder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser");
    //Searching for the dataset
    private final File[] file = folder.listFiles((dir, name) -> name.endsWith(".csv"));
    //Creating the variable where the data from the dataset will be stored
    protected final String[][] dataset = new String[(int) Files.lines(file[0].toPath()).filter(fileLine -> fileLine.contains("Total")).count() - 4][3];
    //Creating the variable where the year and the pay gap will be stored
    protected final String[][] genderPayGap = new String[dataset.length / 2][2];
    //Creating the variable where the dataset and its predictions are stored
    protected String[][] datasetWithPredictions;
    //Creating the variable where the gender pay gaps and the predictions are stored
    protected String[][] genderPayGapWithPredictions;
    //Variables where the men's and women's wages evolution and the pay gap evolution analyses will be stored
    protected String womenAnalysis = "";
    protected String menAnalysis = "";
    protected String wageGapAnalysis = "";
    protected String womenAnalysisWithPredictions = "";
    protected String menAnalysisWithPredictions = "";
    protected String wageGapAnalysisWithPredictions = "";
    //Boolean used to know which data to display on the graph page
    protected boolean predictionsGenerated = false;
    protected boolean PDFGeneratedWithPredictions = false;
    protected boolean changedLanguage = false;

    //Constructor
    public DataProcessing() throws IOException {}

    //Function that prepares a usable dataset
    public void prepareData() throws IOException {
        //Initializing the file reader, the temporary dataset array, the position counters used to specify where new data is inserted in the dataset and pay gap set arrays and the variable where the current line read from the file is stored
        BufferedReader readDataset = new BufferedReader(new FileReader(file[0]));
        String[][] temporaryDataArray = new String[(int) Files.lines(file[0].toPath()).filter(line -> line.contains("Total")).count()][3];
        int positionDataset = 0;
        int positionPayGapSet = 0;
        String line;
        //Initializing the variables where the salaries of men and women will be sored per year, used when building the wage gap array and to manage the multiple values for the years 2013 & 2017
        int manSalary = 0;
        int womanSalary = 0;

        //Unpacking the dataset
        //Looping through the file, selecting all the lines that contain the salary for the total population by gender, not accounting the ethnicity and race, and storing them in a temporary array
        while ((line = readDataset.readLine()) != null) {
            if (line.contains("Total")) {
                String[] parts = line.split(",");
                String salary;
                if (parts.length == 6) {
                    salary = parts[4] + parts[5];
                }
                else {
                    salary = parts[4];
                }
                salary = salary.substring(1, salary.length() - 1);
                if (parts[2].equals("Total Women")) {
                    temporaryDataArray[positionDataset][0] = "Women";
                    temporaryDataArray[positionDataset][1] = parts[3];
                    temporaryDataArray[positionDataset][2] = salary;
                } else if (parts[2].equals("Total Men")) {
                    temporaryDataArray[positionDataset][0] = "Men";
                    temporaryDataArray[positionDataset][1] = parts[3];
                    temporaryDataArray[positionDataset][2] = salary;
                }
                //Incrementing the index used by the dataset so that new data does not overwrite already stored data or the data isn't stored with gaps between entries
                positionDataset++;
            }
        }
        //Sorting the temporary dataset array by the year column
        Arrays.sort(temporaryDataArray, Comparator.comparingInt(year -> Integer.parseInt(year[1])));

        //Because we have 2 salary values for the years 2013 & 2017, we loop through the temporary dataset and pay gap set arrays and select the highest value of the salary when we reach the years 2013 and 2017, and for the rest of the years we store them in the final dataset array as they are
        positionDataset = 0;
        for (String[] statistic : temporaryDataArray) {
            if (statistic[1].equals("2013") || statistic[1].equals("2017")) {
                if (statistic[0].equals("Women") && womanSalary == 0) womanSalary = Integer.parseInt(statistic[2]);
                else if (statistic[0].equals("Men") && manSalary == 0) manSalary = Integer.parseInt(statistic[2]);
                else if (statistic[0].equals("Women") && womanSalary != 0 && Integer.parseInt(statistic[2]) > womanSalary) {
                    dataset[positionDataset][0] = statistic[0];
                    dataset[positionDataset][1] = statistic[1];
                    dataset[positionDataset][2] = statistic[2];
                    womanSalary = 0;
                    positionDataset++;
                } else if (statistic[0].equals("Women") && womanSalary != 0 && Integer.parseInt(statistic[2]) <= womanSalary) {
                    dataset[positionDataset][0] = statistic[0];
                    dataset[positionDataset][1] = statistic[1];
                    dataset[positionDataset][2] = String.valueOf(womanSalary);
                    womanSalary = 0;
                    positionDataset++;
                } else if (statistic[0].equals("Men") && manSalary != 0 && Integer.parseInt(statistic[2]) > manSalary) {
                    dataset[positionDataset][0] = statistic[0];
                    dataset[positionDataset][1] = statistic[1];
                    dataset[positionDataset][2] = statistic[2];
                    manSalary = 0;
                    positionDataset++;
                } else if (statistic[0].equals("Men") && manSalary != 0 && Integer.parseInt(statistic[2]) <= manSalary) {
                    dataset[positionDataset][0] = statistic[0];
                    dataset[positionDataset][1] = statistic[1];
                    dataset[positionDataset][2] = String.valueOf(manSalary);
                    manSalary = 0;
                    positionDataset++;
                }
            } else {
                dataset[positionDataset][0] = statistic[0];
                dataset[positionDataset][1] = statistic[1];
                dataset[positionDataset][2] = statistic[2];
                positionDataset++;
            }
        }
        for (String[] statistic : dataset) {
            //Populating the gender pay gap map
            if (statistic[0].equals("Women")) womanSalary = Integer.parseInt(statistic[2]);
            else if (statistic[0].equals("Men")) manSalary = Integer.parseInt(statistic[2]);
            if (womanSalary != 0 && manSalary != 0) {
                genderPayGap[positionPayGapSet][0] = statistic[1];
                genderPayGap[positionPayGapSet][1] = String.valueOf(Math.abs(womanSalary - manSalary));
                positionPayGapSet++;
                womanSalary = 0;
                manSalary = 0;
            }
        }

        //We are done with the dataset CSV file, so we close it since we won't need it anymore
        readDataset.close();
        //Now we have a fully usable dataset stored in the "data" array, containing salaries from 1960 to 2021

        //Generating interpretations of the dataset and the computed yearly wage gaps
        performAnalysis();

        //Creating the salary graphs
        createSalaryGraphForEverybody();
    }

    //Function that generates interpretations of the evolutions of salaries and of the wage gap. It also takes into account generated predictions, if there are any
    public void performAnalysis() {
        //Arrays where the years, their respective salaries and their computed differences are stored
        List<Integer> years = new ArrayList<>();
        List<Integer> womenSalaries = new ArrayList<>();
        List<Integer> menSalaries = new ArrayList<>();
        List<Integer> payGapsArray = new ArrayList<>();
        //Arrays used to store the record highest salaries and the years when they happened
        int[] peakWomen = new int[2];
        int[] peakMen = new int[2];
        //Arrays used to store the years when the salaries kept rising for a minimum of 3 years
        ArrayList<Integer> risingYearsWomen = new ArrayList<>();
        ArrayList<Integer> risingYearsMen = new ArrayList<>();
        //Arrays used to store the sums when the salaries kept rising for a minimum of 3 years
        ArrayList<Integer> risingSalariesWomen = new ArrayList<>();
        ArrayList<Integer> risingSalariesMen = new ArrayList<>();
        //Arrays used to store the years and the sums when the pay gap kept decreasing for a minimum of 3 years
        ArrayList<Integer> dippingYearsPayGap = new ArrayList<>();
        ArrayList<Integer> dippingSumsPayGap = new ArrayList<>();
        //Counters used to check if a period is made up of minimum 3 years or not
        int countRisingYearsWomen = 0;
        int countRisingYearsMen = 0;
        int countDippingYearsPayGap = 0;
        //Counter used to count how long is an evolution period
        int countEvolutionPeriod = 0;
        //Variable where the average pay rise per year will be stored
        int avgPayRise = 0;
        //Variable where the last year of each period of evolution is temporarily stored
        int tempLastYear = 0;
        //Variable where each period of evolution is temporarily stored
        String tempPeriod = "";

        //If the user generated predictions
        if (predictionsGenerated) {
            //Setting the analysis variables to their initial state
            womenAnalysisWithPredictions = Main.language.equals("EN") ? "The peak salary was at " : Main.language.equals("FR") ? "Le plus grand salaire était de " : "Cel mai mare salariu a fost de ";
            menAnalysisWithPredictions = Main.language.equals("EN") ? "The peak salary was at " : Main.language.equals("FR") ? "Le plus grand salaire était de " : "Cel mai mare salariu a fost de ";
            wageGapAnalysisWithPredictions = Main.language.equals("EN") ? "The lowest pay gap was of " : Main.language.equals("FR") ? "La plus petite différence de la paye était de " : "Cea mai mică diferență între salarii a fost de ";

            //Sorting the dataset by salaries, lowest to highest, for the peaks
            Arrays.sort(datasetWithPredictions, Comparator.comparingInt(salary -> Integer.parseInt(salary[2])));

            //Iterating through the dataset in reverse order
            for (int i = datasetWithPredictions.length - 1; i > 0; i--) {
                //If the first positions of both of the peak arrays, which would be the years, are not 0, then the peaks were saved, and we're done iterating
                if (peakWomen[0] != 0 && peakMen[0] != 0)
                    break;

                //If they are 0, then the peaks weren't found and saved yet, so we proceed with the lookup and memorisation
                if (peakWomen[0] == 0 && datasetWithPredictions[i][0].equals("Women")) {
                    peakWomen[0] = Integer.parseInt(datasetWithPredictions[i][1]);
                    peakWomen[1] = Integer.parseInt(datasetWithPredictions[i][2]);
                } else if (peakMen[0] == 0 && datasetWithPredictions[i][0].equals("Men")) {
                    peakMen[0] = Integer.parseInt(datasetWithPredictions[i][1]);
                    peakMen[1] = Integer.parseInt(datasetWithPredictions[i][2]);
                }
            }

            //Sorting the dataset back by years, lowest to highest
            Arrays.sort(datasetWithPredictions, Comparator.comparingInt(year -> Integer.parseInt(year[1])));

            //Sorting the pay gap set by sum, lowest to highest
            Arrays.sort(genderPayGapWithPredictions, Comparator.comparingInt(gap -> Integer.parseInt(gap[1])));

            //Completing the gender analyses with the peaks, then starting to specify the evolution timeframes
            womenAnalysisWithPredictions += formatSalary(String.valueOf(peakWomen[1])) + (Main.language.equals("EN") ? " in the year " : Main.language.equals("FR") ? " dans l'an " : " în anul ") + peakWomen[0] + (Main.language.equals("EN") ? ".\nTimeframes when the salaries kept rising:\n• " : Main.language.equals("FR") ? ".\nPériodes quand les salaires étaient en croissance:\n• " : ".\nPerioade în care salariile au fost în creștere:\n• ");
            menAnalysisWithPredictions += formatSalary(String.valueOf(peakMen[1])) + (Main.language.equals("EN") ? " in the year " : Main.language.equals("FR") ? " dans l'an " : " în anul ") + peakMen[0] + (Main.language.equals("EN") ? ".\nTimeframes when the salaries kept rising:\n• " : Main.language.equals("FR") ? ".\nPériodes quand les salaires étaient en croissance:\n• " : ".\nPerioade în care salariile au fost în creștere:\n• ");
            wageGapAnalysisWithPredictions += formatSalary(genderPayGapWithPredictions[0][1]) + (Main.language.equals("EN") ? " in the year " : Main.language.equals("FR") ? " dans l'an " : " în anul ") + genderPayGapWithPredictions[0][0] + (Main.language.equals("EN") ? ".\nTimeframes when the wage gap kept closing:\n• " : Main.language.equals("FR") ? ".\nPériodes quand la différence de la paye était en décroissance:\n• " : ".\nPerioade în care diferența între salarii a fost în scădere:\n• ");

            //Sorting the pay gap set by years, lowest to highest
            Arrays.sort(genderPayGapWithPredictions, Comparator.comparingInt(year -> Integer.parseInt(year[0])));
        }
        else {
            //Setting the analysis variables to their initial state
            womenAnalysis = Main.language.equals("EN") ? "The peak salary was at " : Main.language.equals("FR") ? "Le plus grand salaire était de " : "Cel mai mare salariu a fost de ";
            menAnalysis = Main.language.equals("EN") ? "The peak salary was at " : Main.language.equals("FR") ? "Le plus grand salaire était de " : "Cel mai mare salariu a fost de ";
            wageGapAnalysis = Main.language.equals("EN") ? "The lowest pay gap was of " : Main.language.equals("FR") ? "La plus petite différence de la paye était de " : "Cea mai mică diferență între salarii a fost de ";
            //Sorting the dataset by salaries, lowest to highest, for the peaks
            Arrays.sort(dataset, Comparator.comparingInt(salary -> Integer.parseInt(salary[2])));

            //Iterating through the dataset in reverse order
            for (int i = dataset.length - 1; i > 0; i--) {
                //If the first positions of both of the peak arrays, which would be the years, are not 0, then the peaks were saved, and we're done iterating
                if (peakWomen[0] != 0 && peakMen[0] != 0)
                    break;

                //If they are 0, then the peaks weren't found and saved yet, so we proceed with the lookup and memorization
                if (peakWomen[0] == 0 && dataset[i][0].equals("Women")) {
                    peakWomen[0] = Integer.parseInt(dataset[i][1]);
                    peakWomen[1] = Integer.parseInt(dataset[i][2]);
                } else if (peakMen[0] == 0 && dataset[i][0].equals("Men")) {
                    peakMen[0] = Integer.parseInt(dataset[i][1]);
                    peakMen[1] = Integer.parseInt(dataset[i][2]);
                }
            }

            //Sorting the dataset back by years, lowest to highest
            Arrays.sort(dataset, Comparator.comparingInt(year -> Integer.parseInt(year[1])));

            //Sorting the pay gap set by sum, lowest to highest
            Arrays.sort(genderPayGap, Comparator.comparingInt(gap -> Integer.parseInt(gap[1])));

            //Completing the gender analyses with the peaks, then starting to specify the evolution timeframes
            womenAnalysis += formatSalary(String.valueOf(peakWomen[1])) + (Main.language.equals("EN") ? " in the year " : Main.language.equals("FR") ? " dans l'an " : " în anul ") + peakWomen[0] + (Main.language.equals("EN") ? ".\nTimeframes when the salaries kept rising:\n• " : Main.language.equals("FR") ? ".\nPériodes quand les salaires étaient en croissance:\n• " : ".\nPerioade în care salariile au fost în creștere:\n• ");
            menAnalysis += formatSalary(String.valueOf(peakMen[1])) + (Main.language.equals("EN") ? " in the year " : Main.language.equals("FR") ? " dans l'an " : " în anul ") + peakMen[0] + (Main.language.equals("EN") ? ".\nTimeframes when the salaries kept rising:\n• " : Main.language.equals("FR") ? ".\nPériodes quand les salaires étaient en croissance:\n• " : ".\nPerioade în care salariile au fost în creștere:\n• ");
            wageGapAnalysis += formatSalary(genderPayGap[0][1]) + (Main.language.equals("EN") ? " in the year " : Main.language.equals("FR") ? " dans l'an " : " în anul ") + genderPayGap[0][0] + (Main.language.equals("EN") ? ".\nTimeframes when the wage gap kept closing:\n• " : Main.language.equals("FR") ? ".\nPériodes quand la différence de la paye était en décroissance:\n• " : ".\nPerioade în care diferența între salarii a fost în scădere:\n• ");

            //Sorting the pay gap set by years, lowest to highest
            Arrays.sort(genderPayGap, Comparator.comparingInt(year -> Integer.parseInt(year[0])));
        }

        //Separating the dataset in years, men's salaries and women's salaries
        for (String[] stats : predictionsGenerated ? datasetWithPredictions : dataset) {
            if (!years.contains(Integer.parseInt(stats[1]))) years.add(Integer.parseInt(stats[1]));
            if (stats[0].equals("Women")) womenSalaries.add(Integer.parseInt(stats[2]));
            else menSalaries.add(Integer.parseInt(stats[2]));
        }
        for (String[] gap : predictionsGenerated ? genderPayGapWithPredictions : genderPayGap) {
            payGapsArray.add(Integer.parseInt(gap[1]));
        }

        //Adding a -1 in all the rising and dipping arrays to help with initial comparison
        risingYearsWomen.add(-1);
        risingYearsMen.add(-1);
        dippingYearsPayGap.add(-1);
        risingSalariesWomen.add(-1);
        risingSalariesMen.add(-1);
        dippingSumsPayGap.add(-1);

        //Iterating through the salary arrays and memorizing the periods of 3 or more years when the salaries continuously rose
        for (int i = 0; i < years.size(); i++) {
            //Checking for women
            //If the current salary is bigger than the last salary added to the array that stores women's evolution periods' salaries
            if (womenSalaries.get(i) > risingSalariesWomen.get(risingSalariesWomen.size() - 1)) {
                //We add the current salary to the array that stores women's evolution periods' salaries
                risingSalariesWomen.add(womenSalaries.get(i));
                //We add the current year to the array that stores women's evolution periods
                risingYearsWomen.add(years.get(i));
                //We increment the counter that counts how long the women's current evolution period is
                countRisingYearsWomen++;
            }
            //If the current salary is smaller than the last salary added to the array that stores women's evolution periods' salaries and the current evolution period is of 3 or more years, then we end the current evolution period by adding the end marker (that being -1) and resetting the counter that counts how long the current women's evolution period is
            else if (womenSalaries.get(i) < risingSalariesWomen.get(risingSalariesWomen.size() - 1) && countRisingYearsWomen >= 3) {
                risingSalariesWomen.add(-1);
                risingYearsWomen.add(-1);
                countRisingYearsWomen = 0;
            }
            //If the current salary is smaller than the last salary added to the array that stores women's evolution periods' salaries and the current evolution period is not of 3 or more years
            else if (womenSalaries.get(i) < risingSalariesWomen.get(risingSalariesWomen.size() - 1) && countRisingYearsWomen < 3) {
                //Removing the one or 2 salaries and years that were added in the rising salaries and years arrays, until we find the initial -1 or the -1 that marks the end of the last rising period
                while (risingYearsWomen.get(risingYearsWomen.size() - 1) != -1) {
                    risingYearsWomen.remove(risingYearsWomen.size() - 1);
                    risingSalariesWomen.remove(risingSalariesWomen.size() - 1);
                }
                //We add the current salary and year, and we set the counter that counts how long the current evolution period is to 1 since we started a new evolution period that currently contains one year and one salary
                risingSalariesWomen.add(womenSalaries.get(i));
                risingYearsWomen.add(years.get(i));
                countRisingYearsWomen = 1;
            }

            //Doing the same operations as above for men's and wage gap's evolutions, but using their respective arrays and counters
            //Checking for men
            if (menSalaries.get(i) > risingSalariesMen.get(risingSalariesMen.size() - 1)) {
                risingSalariesMen.add(menSalaries.get(i));
                risingYearsMen.add(years.get(i));
                countRisingYearsMen++;
            } else if (menSalaries.get(i) < risingSalariesMen.get(risingSalariesMen.size() - 1) && countRisingYearsMen >= 3) {
                risingSalariesMen.add(-1);
                risingYearsMen.add(-1);
                countRisingYearsMen = 0;
            } else if (menSalaries.get(i) < risingSalariesMen.get(risingSalariesMen.size() - 1) && countRisingYearsMen < 3) {
                //Removing the one or 2 salaries and years that were added in the rising salaries and years arrays, until we find the initial -1 or the -1 that marks the end of the last rising period
                while (risingYearsMen.get(risingYearsMen.size() - 1) != -1) {
                    risingYearsMen.remove(risingYearsMen.size() - 1);
                    risingSalariesMen.remove(risingSalariesMen.size() - 1);
                }
                risingSalariesMen.add(menSalaries.get(i));
                risingYearsMen.add(years.get(i));
                countRisingYearsMen = 1;
            }

            //Checking for pay gap
            if (payGapsArray.get(i) < dippingSumsPayGap.get(dippingSumsPayGap.size() - 1)) {
                dippingSumsPayGap.add(payGapsArray.get(i));
                dippingYearsPayGap.add(years.get(i));
                countDippingYearsPayGap++;
            } else if (payGapsArray.get(i) > dippingSumsPayGap.get(dippingSumsPayGap.size() - 1) && countDippingYearsPayGap >= 3) {
                dippingSumsPayGap.add(-1);
                dippingYearsPayGap.add(-1);
                countDippingYearsPayGap = 0;
            } else if (payGapsArray.get(i) > dippingSumsPayGap.get(dippingSumsPayGap.size() - 1) && countDippingYearsPayGap < 3) {
                //Removing the one or 2 salaries and years that were added in the rising salaries and years arrays, until we find the initial -1 or the -1 that marks the end of the last rising period
                while (dippingYearsPayGap.get(dippingYearsPayGap.size() - 1) != -1) {
                    dippingYearsPayGap.remove(dippingYearsPayGap.size() - 1);
                    dippingSumsPayGap.remove(dippingSumsPayGap.size() - 1);
                }
                dippingSumsPayGap.add(payGapsArray.get(i));
                dippingYearsPayGap.add(years.get(i));
                countDippingYearsPayGap = 1;
            }
        }

        //Removing the first -1s from the rising arrays since they're no longer needed and will pose problems later on
        risingYearsWomen.remove(0);
        risingYearsMen.remove(0);
        dippingYearsPayGap.remove(0);
        risingSalariesWomen.remove(0);
        risingSalariesMen.remove(0);
        dippingSumsPayGap.remove(0);

        //Cleaning the incomplete evolution periods in the rising and dipping arrays, if there are any, or completing them if the iterations are done before the end marker being added
        if (countRisingYearsWomen >= 3) {
            risingYearsWomen.add(-1);
            risingSalariesWomen.add(-1);
        } else {
            while (risingYearsWomen.get(risingYearsWomen.size() - 1) != -1) {
                risingYearsWomen.remove(risingYearsWomen.size() - 1);
                risingSalariesWomen.remove(risingSalariesWomen.size() - 1);
            }
        }
        if (countRisingYearsMen >= 3) {
            risingYearsMen.add(-1);
            risingSalariesMen.add(-1);
        } else {
            while (risingYearsMen.get(risingYearsMen.size() - 1) != -1) {
                risingYearsMen.remove(risingYearsMen.size() - 1);
                risingSalariesMen.remove(risingSalariesMen.size() - 1);
            }
        }
        if (countDippingYearsPayGap >= 3) {
            dippingYearsPayGap.add(-1);
            dippingSumsPayGap.add(-1);
        } else {
            while (dippingYearsPayGap.get(dippingYearsPayGap.size() - 1) != -1) {
                dippingYearsPayGap.remove(dippingYearsPayGap.size() - 1);
                dippingSumsPayGap.remove(dippingSumsPayGap.size() - 1);
            }
        }

        //Completing the women analysis with the periods of pay rise and yearly average
        for (int i = 0; i < risingYearsWomen.size(); i++) {
            //If the evolution period is not over
            if (risingYearsWomen.get(i) != -1) {
                //If we didn't start formatting the evolution period yet
                if (tempPeriod.equals("")) {
                    //We start the formatted period with the first year of the evolution period
                    tempPeriod = risingYearsWomen.get(i) + " - ";
                    //We calculate the difference between the wage of the first 2 years in the evolution period
                    avgPayRise = risingSalariesWomen.get(i + 1) - risingSalariesWomen.get(i);
                }
                //If we did start formatting the evolution period already
                else {
                    //We save the next year in the temporary year variable, to be used as the last year of the evolution period in the formatted period, in case the next year is -1 which marks the end of the evolution period
                    tempLastYear = risingYearsWomen.get(i);
                    //If the next year is not the end marker (-1)
                    if (risingSalariesWomen.get(i + 1) != -1)
                        //We calculate the difference between the wages of the current year and the next one
                        avgPayRise += risingSalariesWomen.get(i + 1) - risingSalariesWomen.get(i);
                }
                //We count the years in the evolution period
                countEvolutionPeriod++;
            }
            //If the evolution period is over
            else {
                //We finish the formatted evolution period with the last year of the evolution period
                tempPeriod += tempLastYear;
                if (predictionsGenerated) {
                    //We complete the analysis with this period and the average yearly pay rise, which is the sum of the yearly differences divided by the number of years - 1
                    womenAnalysisWithPredictions += tempPeriod + (Main.language.equals("EN") ? " with an average yearly pay rise of " : Main.language.equals("FR") ? " avec une croissance moyenne salariale par an de " : " cu o creștere salarială medie pe an de ") + formatSalary(String.valueOf(Math.abs(avgPayRise / (countEvolutionPeriod - 1))));
                    //We clear the formatted evolution period to begin the next one
                    tempPeriod = "";
                    //We clear the counted years of the currently finished evolution period to begin the next one
                    countEvolutionPeriod = 0;
                    //If we don't have any more evolutions
                    if (i == risingYearsWomen.size() - 1)
                        //We end the analysis
                        womenAnalysisWithPredictions += ".";
                        //If we have more evolutions
                    else
                        //We continue the analysis with the next evolution period
                        womenAnalysisWithPredictions += ";\n• ";
                } else {
                    //We complete the analysis with this period and the average yearly pay rise, which is the sum of the yearly differences divided by the number of years - 1
                    womenAnalysis += tempPeriod + (Main.language.equals("EN") ? " with an average yearly pay rise of " : Main.language.equals("FR") ? " avec une croissance moyenne salariale par an de " : " cu o creștere salarială medie pe an de ") + formatSalary(String.valueOf(Math.abs(avgPayRise / (countEvolutionPeriod - 1))));
                    //We clear the formatted evolution period to begin the next one
                    tempPeriod = "";
                    //We clear the counted years of the currently finished evolution period to begin the next one
                    countEvolutionPeriod = 0;
                    //If we don't have any more evolutions
                    if (i == risingYearsWomen.size() - 1)
                        //We end the analysis
                        womenAnalysis += ".";
                        //If we have more evolutions
                    else
                        //We continue the analysis with the next evolution period
                        womenAnalysis += ";\n• ";
                }
            }
        }

        //Completing the men analysis with the periods of pay rise and yearly average
        for (int i = 0; i < risingYearsMen.size(); i++) {
            //If the evolution period is not over
            if (risingYearsMen.get(i) != -1) {
                //If we didn't start formatting the evolution period yet
                if (tempPeriod.equals("")) {
                    //We start the formatted period with the first year of the evolution period
                    tempPeriod = risingYearsMen.get(i) + " - ";
                    //We calculate the difference between the wage of the first 2 years in the evolution period
                    avgPayRise = risingSalariesMen.get(i + 1) - risingSalariesMen.get(i);
                }
                //If we did start formatting the evolution period already
                else {
                    //We save the next year in the temporary year variable, to be used as the last year of the evolution period in the formatted period, in case the next year is -1 which marks the end of the evolution period
                    tempLastYear = risingYearsMen.get(i);
                    //If the next year is not the end marker (-1)
                    if (risingSalariesMen.get(i + 1) != -1)
                        //We calculate the difference between the wages of the current year and the next one
                        avgPayRise += risingSalariesMen.get(i + 1) - risingSalariesMen.get(i);
                }
                //We count the years in the evolution period
                countEvolutionPeriod++;
            }
            //If the evolution period is over
            else {
                //We finish the formatted evolution period with the last year of the evolution period
                tempPeriod += tempLastYear;
                if (predictionsGenerated) {
                    //We complete the analysis with this period and the average yearly pay rise, which is the sum of the yearly differences divided by the number of years - 1
                    menAnalysisWithPredictions += tempPeriod + (Main.language.equals("EN") ? " with an average yearly pay rise of " : Main.language.equals("FR") ? " avec une croissance moyenne salariale par an de " : " cu o creștere salarială medie pe an de ") + formatSalary(String.valueOf(Math.abs(avgPayRise / countEvolutionPeriod)));
                    //We clear the formatted evolution period to begin the next one
                    tempPeriod = "";
                    //We clear the counted years of the currently finished evolution period to begin the next one
                    countEvolutionPeriod = 0;
                    //If we don't have any more evolutions
                    if (i == risingYearsMen.size() - 1)
                        //We end the analysis
                        menAnalysisWithPredictions += ".";
                        //If we have more evolutions
                    else
                        //We continue the analysis with the next evolution period
                        menAnalysisWithPredictions += ";\n• ";
                } else {
                    //We complete the analysis with this period and the average yearly pay rise, which is the sum of the yearly differences divided by the number of years - 1
                    menAnalysis += tempPeriod + (Main.language.equals("EN") ? " with an average yearly pay rise of " : Main.language.equals("FR") ? " avec une croissance moyenne salariale par an de " : " cu o creștere salarială medie pe an de ") + formatSalary(String.valueOf(Math.abs(avgPayRise / countEvolutionPeriod)));
                    //We clear the formatted evolution period to begin the next one
                    tempPeriod = "";
                    //We clear the counted years of the currently finished evolution period to begin the next one
                    countEvolutionPeriod = 0;
                    //If we don't have any more evolutions
                    if (i == risingYearsMen.size() - 1)
                        //We end the analysis
                        menAnalysis += ".";
                        //If we have more evolutions
                    else
                        //We continue the analysis with the next evolution period
                        menAnalysis += ";\n• ";
                }
            }
        }

        //Completing the pay gap analysis with the periods of gap shrinkage and yearly average
        //We're reusing the avgPayRise variable to store the sum of the differences between the yearly wage gaps for each evolution period
        for (int i = 0; i < dippingYearsPayGap.size(); i++) {
            //If the evolution period is not over
            if (dippingYearsPayGap.get(i) != -1) {
                //If we didn't start formatting the evolution period yet
                if (tempPeriod.equals("")) {
                    //We start the formatted period with the first year of the evolution period
                    tempPeriod = dippingYearsPayGap.get(i) + " - ";
                    //We calculate the difference between the wage gaps of the first 2 years in the evolution period
                    avgPayRise = dippingSumsPayGap.get(i) - dippingSumsPayGap.get(i + 1);
                }
                //If we did start formatting the evolution period already
                else {
                    //We save the next year in the temporary year variable, to be used as the last year of the evolution period in the formatted period, in case the next year is -1 which marks the end of the evolution period
                    tempLastYear = dippingYearsPayGap.get(i);
                    //If the next year is not the end marker (-1)
                    if (dippingSumsPayGap.get(i + 1) != -1)
                        //We calculate the difference between the wage gaps of the current year and the next one
                        avgPayRise += dippingSumsPayGap.get(i) - dippingSumsPayGap.get(i + 1);
                }
                //We count the years in the evolution period
                countEvolutionPeriod++;
            }
            //If the evolution period is over
            else {
                //We finish the formatted evolution period with the last year of the evolution period
                tempPeriod += tempLastYear;
                if (predictionsGenerated) {
                    //We complete the analysis with this period and the average yearly pay rise, which is the sum of the yearly differences divided by the number of years - 1
                    wageGapAnalysisWithPredictions += tempPeriod + (Main.language.equals("EN") ? " with an average yearly fall of " : Main.language.equals("FR") ? " avec une décroissance moyenne salariale par an de " : " cu o scădere medie pe an de ") + formatSalary(String.valueOf(avgPayRise / countEvolutionPeriod));
                    //We clear the formatted evolution period to begin the next one
                    tempPeriod = "";
                    //We clear the counted years of the currently finished evolution period to begin the next one
                    countEvolutionPeriod = 0;
                    //If we don't have any more evolutions
                    if (i == dippingYearsPayGap.size() - 1)
                        //We end the analysis
                        wageGapAnalysisWithPredictions += ".";
                        //If we have more evolutions
                    else
                        //We continue the analysis with the next evolution period
                        wageGapAnalysisWithPredictions += ";\n• ";
                } else {
                    //We complete the analysis with this period and the average yearly pay rise, which is the sum of the yearly differences divided by the number of years - 1
                    wageGapAnalysis += tempPeriod + (Main.language.equals("EN") ? " with an average yearly fall of " : Main.language.equals("FR") ? " avec une décroissance moyenne salariale par an de " : " cu o scădere medie pe an de ") + formatSalary(String.valueOf(avgPayRise / countEvolutionPeriod));
                    //We clear the formatted evolution period to begin the next one
                    tempPeriod = "";
                    //We clear the counted years of the currently finished evolution period to begin the next one
                    countEvolutionPeriod = 0;
                    //If we don't have any more evolutions
                    if (i == dippingYearsPayGap.size() - 1)
                        //We end the analysis
                        wageGapAnalysis += ".";
                        //If we have more evolutions
                    else
                        //We continue the analysis with the next evolution period
                        wageGapAnalysis += ";\n• ";
                }
            }
        }
        //Changing the analyses to include a final conclusion justifying the rest of time, between all the evolutions
        if (predictionsGenerated) {
            womenAnalysisWithPredictions += Main.language.equals("EN") ? "\nBetween these evolution periods, the salaries either dropped until the next evolution period, or rose from one year to the next and then dropped." : Main.language.equals("FR") ? "\nEntre ces périodes évolutifs, les salaires ont tombé jusqu'à la prochaine période évolutive, ou ont augmenté juste d'un an a l'autre et après tombé." : "\nÎntre aceste perioade de evoluție, salariile ori au scăzut până la următoarea perioadă de evoluție, ori au crescut doar de la un an la următorul iar apoi au scăzut.";
            menAnalysisWithPredictions += Main.language.equals("EN") ? "\nBetween these evolution periods, the salaries either dropped until the next evolution period, or rose from one year to the next and then dropped." : Main.language.equals("FR") ? "\nEntre ces périodes évolutifs, les salaires ont tombé jusqu'à la prochaine période évolutive, ou ont augmenté juste d'un an a l'autre et après tombé." : "\nÎntre aceste perioade de evoluție, salariile ori au scăzut până la următoarea perioadă de evoluție, ori au crescut doar de la un an la următorul iar apoi au scăzut.";
            wageGapAnalysisWithPredictions += Main.language.equals("EN") ? "\nBetween these evolution periods, the wage gap either kept growing until the next evolution period, or it got smaller from one year to the next and then grew." : Main.language.equals("FR") ? "\nEntre ces périodes évolutifs, la différence de la paye a augmenté jusqu'à la prochaine période évolutive, ou elle a tombé juste d'un an à l'autre et après augmenté." : "\nÎntre aceste perioade de evoluție, diferența între salarii ori a crescut până la următoarea perioadă de evoluție, ori a scăzut doar de la un an la următorul iar apoi a crescut.";
        } else {
            womenAnalysis += Main.language.equals("EN") ? "\nBetween these evolution periods, the salaries either dropped until the next evolution period, or rose from one year to the next and then dropped." : Main.language.equals("FR") ? "\nEntre ces périodes évolutifs, les salaires ont tombé jusqu'à la prochaine période évolutive, ou ont augmenté juste d'un an à l'autre et après tombé." : "\nÎntre aceste perioade de evoluție, salariile ori au scăzut până la următoarea perioadă de evoluție, ori au crescut doar de la un an la următorul iar apoi au scăzut.";
            menAnalysis += Main.language.equals("EN") ? "\nBetween these evolution periods, the salaries either dropped until the next evolution period, or rose from one year to the next and then dropped." : Main.language.equals("FR") ? "\nEntre ces périodes évolutifs, les salaires ont tombé jusqu'à la prochaine période évolutive, ou ont augmenté juste d'un an à l'autre et après tombé." : "\nÎntre aceste perioade de evoluție, salariile ori au scăzut până la următoarea perioadă de evoluție, ori au crescut doar de la un an la următorul iar apoi au scăzut.";
            wageGapAnalysis += Main.language.equals("EN") ? "\nBetween these evolution periods, the wage gap either kept growing until the next evolution period, or it got smaller from one year to the next and then grew." : Main.language.equals("FR") ? "\nEntre ces périodes évolutifs, la différence de la paye a augmenté jusqu'à la prochaine période évolutive, ou elle à tombé juste d'un an à l'autre et après augmenté." : "\nÎntre aceste perioade de evoluție, diferența între salarii ori a crescut până la următoarea perioadă de evoluție, ori a scăzut doar de la un an la următorul iar apoi a crescut.";
        }
    }

    //Helper function to format the salary from just a number to a string with the $ sign and periods - cosmetic purposes
    public String formatSalary(String rawSalary) {
        //String where the formatted number (containing the periods before every group of 3 digits) will be stored
        String formattedSalaryString = "";
        //If the salary is only 2 digits long, we leave it as-is, but attach a $ sign to its beginning
        if (rawSalary.length() == 1 || rawSalary.length() == 2)
            return '$' + rawSalary;
        //Iterating over the initial salary string, every group of 3 digits (when possible), from the end of the string to the start
        for (int i = rawSalary.length(); i > 0; i -= 3) {
            //If we are at the end of the salary string, we attach the last group of 3 digits to the formatted salary string
            if (i == rawSalary.length())
                formattedSalaryString = rawSalary.substring(i - 3);
            //If we are anywhere else in the string, and we have more than the first one or 2 digits left (if that is the case), we attach the 3-digit group that the digit at the current position is part of, with a period, to the formatted salary string. If the current 3-digit group is the first 3-digit group of the unformatted salary, the loop finishes
            else if (i > 2)
                formattedSalaryString = rawSalary.substring(i - 3, i) + '.' + formattedSalaryString;
            //If the first one or 2 digits (if that is the case) are left to be attached, we attach them with a period to the formatted salary string
            else
                formattedSalaryString = rawSalary.substring(0, i) + '.' + formattedSalaryString;
        }
        //We return the formatted salary string with a $ sign attached to its beginning
        return '$' + formattedSalaryString;
    }

    //Function that predicts the evolutions of the salaries using simple linear regression analysis, and computes the differences between predicted salaries
    public void predictEvolutions(int numberOfYears) throws IOException {
        //Initializing the array
        datasetWithPredictions = new String[dataset.length + numberOfYears * 2][3];
        genderPayGapWithPredictions = new String[genderPayGap.length + numberOfYears][2];

        //Populating the arrays above with the initial menDataset and gender pay gap set values, without predictions
        for (int position = 0; position < dataset.length; position++) {
            datasetWithPredictions[position][0] = dataset[position][0];
            datasetWithPredictions[position][1] = dataset[position][1];
            datasetWithPredictions[position][2] = dataset[position][2];
        }
        for (int position = 0; position < genderPayGap.length; position++) {
            genderPayGapWithPredictions[position][0] = genderPayGap[position][0];
            genderPayGapWithPredictions[position][1] = genderPayGap[position][1];
        }

        //Using regression analysis and forecasting to predict women's salary evolution
        SimpleRegression regressionWomen = new SimpleRegression();
        //Using regression analysis and forecasting to predict men's salary evolution
        SimpleRegression regressionMen = new SimpleRegression();

        //Adding the salaries to the regression models
        for (int i = 0; i < dataset.length; i++) {
            if (dataset[i][0].equals("Women"))
                regressionWomen.addData(i, Double.parseDouble(dataset[i][2]));
            else if (dataset[i][0].equals("Men"))
                regressionMen.addData(i, Double.parseDouble(dataset[i][2]));
        }

        //Saving and incrementing the last year of the initial dataset that is going to be incremented after each prediction is inserted in the array that contains the initial dataset and the prediction
        int lastYear = Integer.parseInt(dataset[dataset.length - 1][1]) + 1;

        //Performing predictions using the slope and intercept of men and women, for however many years the user wants predictions
        for (int i = dataset.length; i < dataset.length + numberOfYears * 2; i += 2) {
            datasetWithPredictions[i][0] = "Women";
            datasetWithPredictions[i][1] = String.valueOf(lastYear);
            datasetWithPredictions[i][2] = String.valueOf((int)regressionWomen.predict(i));

            datasetWithPredictions[i + 1][0] = "Men";
            datasetWithPredictions[i + 1][1] = String.valueOf(lastYear);
            datasetWithPredictions[i + 1][2] = String.valueOf((int)regressionMen.predict(i));

            lastYear++;
        }
        lastYear = Integer.parseInt(genderPayGap[genderPayGap.length - 1][0]) + 1;
        for (int i = genderPayGap.length; i < genderPayGap.length + numberOfYears; i++) {
            genderPayGapWithPredictions[i][0] = String.valueOf(lastYear);
            genderPayGapWithPredictions[i][1] = String.valueOf(Math.abs(Integer.parseInt(datasetWithPredictions[i * 2 + 1][2]) - Integer.parseInt(datasetWithPredictions[i * 2][2])));

            lastYear++;
        }

        //Generating evolution graph of the dataset that includes the predictions
        createSalaryGraphWithPredictionsForEverybody();

        //Setting the predictions generated boolean to true so that the app uses the predictions where applicable
        predictionsGenerated = true;

        //Generating interpretations of the dataset
        performAnalysis();
    }

    //Function that is triggered when the user wishes that the app no longer takes the generated predictions in consideration when displaying the graphs and the interpretations
    public void discardPredictions() {
        //Clearing the array that contains the initial data and the predictions
        datasetWithPredictions = null;
        //Clearing the array that contains the computed differences between the original salaries and between the predicted salaries
        genderPayGapWithPredictions = null;
        //Locating the Graphs folder
        File graphsFolder = new File("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/");
        //Traversing the graphs folder
        for (File graph : Objects.requireNonNull(graphsFolder.listFiles())) {
            //Deleting all the graphs that contain "-prediction" in their names
            if (graph.getName().contains("-prediction"))
                graph.delete();
        }
        //Setting the boolean variable that reflects the presence of predictions to false so that the app doesn't believe in any place that predictions exist
        predictionsGenerated = false;
        //Deleting the analyses that include predictions
        womenAnalysisWithPredictions = "";
        menAnalysisWithPredictions = "";
        wageGapAnalysisWithPredictions = "";
    }

    //Function that creates all the graphs in advance for performance boost when displaying, when the user makes different display choices
    //Uses the Plot class created by YuriY Guskov, found at https://github.com/yuriy-g/simple-java-plot
    public void createSalaryGraphForEverybody() throws IOException {
        double smallestYear = Double.parseDouble(dataset[0][1]);
        double biggestYear = Double.parseDouble(dataset[dataset.length - 1][1]);
        //Separating the dataset into 3 arrays (one with the years, one with men's salaries and one with women's salaries) to use in the plot
        List<Double> years = new ArrayList<>();
        List<Double> womenSalaries = new ArrayList<>();
        List<Double> menSalaries = new ArrayList<>();
        List<Double> allSalaries = new ArrayList<>();
        //Extracting the calculated pay gaps from the map into an array use in the plot
        List<Double> payGapsArray = new ArrayList<>();
        for (String[] gap : genderPayGap) {
            payGapsArray.add(Double.parseDouble(gap[1]));
        }
        for (String[] stats : dataset) {
            if (!years.contains(Double.parseDouble(stats[1]))) years.add(Double.parseDouble(stats[1]));
            allSalaries.add(Double.parseDouble(stats[2]));
            if (stats[0].equals("Women")) womenSalaries.add(Double.parseDouble(stats[2]));
            else menSalaries.add(Double.parseDouble(stats[2]));
        }
        //Determining the smallest and biggest salary to set as range for the Y axis of the plot
        int smallestSalaryEverybody = Collections.min(allSalaries).intValue();
        int biggestSalaryEverybody = Collections.max(allSalaries).intValue();
        int smallestSalaryWomen = Collections.min(womenSalaries).intValue();
        int biggestSalaryWomen = Collections.max(womenSalaries).intValue();
        int smallestSalaryMen = Collections.min(menSalaries).intValue();
        int biggestSalaryMen = Collections.max(menSalaries).intValue();
        int smallestPayGap = Collections.min(payGapsArray).intValue();
        int biggestPayGap = Collections.max(payGapsArray).intValue();
        //Transforming the above variables into string for easier manipulation later
        String smallestSalaryEverybodyString = String.valueOf(smallestSalaryEverybody);
        String biggestSalaryEverybodyString = String.valueOf(biggestSalaryEverybody);
        String smallestSalaryWomenString = String.valueOf(smallestSalaryWomen);
        String biggestSalaryWomenString = String.valueOf(biggestSalaryWomen);
        String smallestSalaryMenString = String.valueOf(smallestSalaryMen);
        String biggestSalaryMenString = String.valueOf(biggestSalaryMen);
        String smallestPayGapString = String.valueOf(smallestPayGap);
        String biggestPayGapString = String.valueOf(biggestPayGap);
        //Generating the plot with all the salaries
        Plot allGendersPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires dans les États Unis" : "Evoluția Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybodyString.length() == 1 ? 0 : smallestSalaryEverybody - Integer.parseInt(smallestSalaryEverybodyString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        Plot allGendersPlotWithGap = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage and Wage Gap Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires et de la Différence de la Paye dans les États Unis" : "Evoluția Salariilor și a Diferenței între Salarii în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybody < smallestPayGap && smallestSalaryEverybodyString.length() == 1 ? 0 : smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green)).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Generating the plot with women's salaries
        Plot womenPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires dans les États Unis" : "Evoluția Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryWomenString.length() == 1 ? 0 : smallestSalaryWomen - Integer.parseInt(smallestSalaryWomenString.substring(1)), biggestSalaryWomenString.length() == 1 ? 10 : biggestSalaryWomen + Math.pow(10, biggestSalaryWomenString.length() - 1) - Integer.parseInt(biggestSalaryWomenString.substring(1)))).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        //Generating the plot with men's salaries
        Plot menPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires dans les États Unis" : "Evoluția Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryMenString.length() == 1 ? 0 : smallestSalaryMen - Integer.parseInt(smallestSalaryMenString.substring(1)), biggestSalaryMenString.length() == 1 ? 10 : biggestSalaryMen + Math.pow(10, biggestSalaryMenString.length() - 1) - Integer.parseInt(biggestSalaryMenString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red));
        //Generating the plot with the gender wage gaps
        Plot wageGapPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires dans les États Unis" : "Evoluția Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestPayGapString.length() == 1 ? 10 : biggestPayGap + Math.pow(10, biggestPayGapString.length() - 1) - Integer.parseInt(biggestPayGapString.substring(1)))).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye'" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Saving the plots to be used in the graph display screen
        allGendersPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders", "png");
        allGendersPlotWithGap.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap", "png");
        womenPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women", "png");
        menPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men", "png");
        wageGapPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap", "png");
        //Now we can generate 5 graphs - one for men's salaries, one for women's salaries, one for both, one for both that also include the wage gap, and one for the wage gap - to be displayed according to the user's choice
    }

    //Function that creates all the graphs containing specified range in advance for performance boost when displaying, when the user makes different display choices
    //Uses the Plot class created by YuriY Guskov, found at https://github.com/yuriy-g/simple-java-plot
    public void createSalaryGraphWithinRangeForEverybody(int minimum, int maximum) throws IOException {
        //Separating the dataset into 3 arrays (one with the years, one with men's salaries and one with women's salaries) to use in the plot
        List<Double> years = new ArrayList<>();
        List<Double> womenSalaries = new ArrayList<>();
        List<Double> menSalaries = new ArrayList<>();
        List<Double> allSalaries = new ArrayList<>();
        //Extracting the calculated pay gaps from the map into an array use in the plot
        List<Double> payGapsArray = new ArrayList<>();
        for (String[] gap : genderPayGap) {
            if (Integer.parseInt(gap[0]) >= minimum && Integer.parseInt(gap[0]) <= maximum)
                payGapsArray.add(Double.parseDouble(gap[1]));
        }
        for (String[] stats : dataset) {
            if (Integer.parseInt(stats[1]) >= minimum && Integer.parseInt(stats[1]) <= maximum) {
                if (!years.contains(Double.parseDouble(stats[1]))) years.add(Double.parseDouble(stats[1]));
                allSalaries.add(Double.parseDouble(stats[2]));
                if (stats[0].equals("Women")) womenSalaries.add(Double.parseDouble(stats[2]));
                else menSalaries.add(Double.parseDouble(stats[2]));
            }
        }
        //Determining the smallest and biggest salary to set as range for the Y axis of the plot
        int smallestSalaryEverybody = Collections.min(allSalaries).intValue();
        int biggestSalaryEverybody = Collections.max(allSalaries).intValue();
        int smallestSalaryWomen = Collections.min(womenSalaries).intValue();
        int biggestSalaryWomen = Collections.max(womenSalaries).intValue();
        int smallestSalaryMen = Collections.min(menSalaries).intValue();
        int biggestSalaryMen = Collections.max(menSalaries).intValue();
        int smallestPayGap = Collections.min(payGapsArray).intValue();
        int biggestPayGap = Collections.max(payGapsArray).intValue();
        //Transforming the above variables into string for easier manipulation later
        String smallestSalaryEverybodyString = String.valueOf(smallestSalaryEverybody);
        String biggestSalaryEverybodyString = String.valueOf(biggestSalaryEverybody);
        String smallestSalaryWomenString = String.valueOf(smallestSalaryWomen);
        String biggestSalaryWomenString = String.valueOf(biggestSalaryWomen);
        String smallestSalaryMenString = String.valueOf(smallestSalaryMen);
        String biggestSalaryMenString = String.valueOf(biggestSalaryMen);
        String smallestPayGapString = String.valueOf(smallestPayGap);
        String biggestPayGapString = String.valueOf(biggestPayGap);
        //Generating the plot with all the salaries
        Plot allGendersPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires dans les États Unis" : "Evoluția Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybodyString.length() == 1 ? 0 : smallestSalaryEverybody - Integer.parseInt(smallestSalaryEverybodyString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        Plot allGendersPlotWithGap = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage and Wage Gap Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires et de la Différence de la Paye dans les États Unis" : "Evoluția Salariilor și a Diferenței între Salarii în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybody < smallestPayGap && smallestSalaryEverybodyString.length() == 1 ? 0 : smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green)).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Generating the plot with women's salaries
        Plot womenPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires dans les États Unis" : "Evoluția Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryWomenString.length() == 1 ? 0 : smallestSalaryWomen - Integer.parseInt(smallestSalaryWomenString.substring(1)), biggestSalaryWomenString.length() == 1 ? 10 : biggestSalaryWomen + Math.pow(10, biggestSalaryWomenString.length() - 1) - Integer.parseInt(biggestSalaryWomenString.substring(1)))).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        //Generating the plot with men's salaries
        Plot menPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution in the United States" : Main.language.equals("FR") ? "Évolution des Salaires dans les États Unis" : "Evoluția Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryMenString.length() == 1 ? 0 : smallestSalaryMen - Integer.parseInt(smallestSalaryMenString.substring(1)), biggestSalaryMenString.length() == 1 ? 10 : biggestSalaryMen + Math.pow(10, biggestSalaryMenString.length() - 1) - Integer.parseInt(biggestSalaryMenString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red));
        //Generating the plot with the gender wage gaps
        Plot wageGapPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Gap Evolution in the United States" : Main.language.equals("FR") ? "Évolution de la Différence de la Paye dans les États Unis" : "Evoluția Diferenței între Salarii în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestPayGapString.length() == 1 ? 10 : biggestPayGap + Math.pow(10, biggestPayGapString.length() - 1) - Integer.parseInt(biggestPayGapString.substring(1)))).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye'" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Saving the plots to be used in the graph display screen
        allGendersPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-range", "png");
        allGendersPlotWithGap.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-range", "png");
        womenPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-range", "png");
        menPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-range", "png");
        wageGapPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-range", "png");
        //Now we can generate 5 graphs - one for men's salaries, one for women's salaries, one for both, one for both that also include the wage gap, and one for the wage gap which include the statistics within the range the user specified - to be displayed according to the user's choice
    }

    //Function that creates all the graphs that also contain the generated predictions in advance for performance boost when displaying, when the user makes different display choices
    //Uses the Plot class created by YuriY Guskov, found at https://github.com/yuriy-g/simple-java-plot
    public void createSalaryGraphWithPredictionsForEverybody() throws IOException {
        double smallestYear = Double.parseDouble(datasetWithPredictions[0][1]);
        double biggestYear = Double.parseDouble(datasetWithPredictions[datasetWithPredictions.length - 1][1]);
        //Separating the dataset into 3 arrays (one with the years, one with men's salaries and one with women's salaries) to use in the plot
        List<Double> years = new ArrayList<>();
        List<Double> womenSalaries = new ArrayList<>();
        List<Double> menSalaries = new ArrayList<>();
        List<Double> allSalaries = new ArrayList<>();
        //Extracting the calculated pay gaps from the map into an array use in the plot
        List<Double> payGapsArray = new ArrayList<>();
        for (String[] gap : genderPayGapWithPredictions) {
            payGapsArray.add(Double.parseDouble(gap[1]));
        }
        for (String[] stats : datasetWithPredictions) {
            if (!years.contains(Double.parseDouble(stats[1]))) years.add(Double.parseDouble(stats[1]));
            allSalaries.add(Double.parseDouble(stats[2]));
            if (stats[0].equals("Women")) womenSalaries.add(Double.parseDouble(stats[2]));
            else menSalaries.add(Double.parseDouble(stats[2]));
        }
        //Determining the smallest and biggest salary to set as range for the Y axis of the plot
        int smallestSalaryEverybody = Collections.min(allSalaries).intValue();
        int biggestSalaryEverybody = Collections.max(allSalaries).intValue();
        int smallestSalaryWomen = Collections.min(womenSalaries).intValue();
        int biggestSalaryWomen = Collections.max(womenSalaries).intValue();
        int smallestSalaryMen = Collections.min(menSalaries).intValue();
        int biggestSalaryMen = Collections.max(menSalaries).intValue();
        int smallestPayGap = Collections.min(payGapsArray).intValue();
        int biggestPayGap = Collections.max(payGapsArray).intValue();
        //Transforming the above variables into string for easier manipulation later
        String smallestSalaryEverybodyString = String.valueOf(smallestSalaryEverybody);
        String biggestSalaryEverybodyString = String.valueOf(biggestSalaryEverybody);
        String smallestSalaryWomenString = String.valueOf(smallestSalaryWomen);
        String biggestSalaryWomenString = String.valueOf(biggestSalaryWomen);
        String smallestSalaryMenString = String.valueOf(smallestSalaryMen);
        String biggestSalaryMenString = String.valueOf(biggestSalaryMen);
        String smallestPayGapString = String.valueOf(smallestPayGap);
        String biggestPayGapString = String.valueOf(biggestPayGap);
        //Generating the plot with all the salaries
        Plot allGendersPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires dans les États Unis" : "Predicția Evoluției Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybodyString.length() == 1 ? 0 : smallestSalaryEverybody - Integer.parseInt(smallestSalaryEverybodyString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        Plot allGendersPlotWithGap = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage and Wage Gap Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires et de la Différence de la Paye dans les États Unis" : "Predicția Evoluției Salariilor și a Diferenței între Salarii în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybody < smallestPayGap && smallestSalaryEverybodyString.length() == 1 ? 0 : smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green)).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Generating the plot with women's salaries
        Plot womenPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires dans les États Unis" : "Predicția Evoluției Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryWomenString.length() == 1 ? 0 : smallestSalaryWomen - Integer.parseInt(smallestSalaryWomenString.substring(1)), biggestSalaryWomenString.length() == 1 ? 10 : biggestSalaryWomen + Math.pow(10, biggestSalaryWomenString.length() - 1) - Integer.parseInt(biggestSalaryWomenString.substring(1)))).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        //Generating the plot with men's salaries
        Plot menPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires dans les États Unis" : "Predicția Evoluției Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryMenString.length() == 1 ? 0 : smallestSalaryMen - Integer.parseInt(smallestSalaryMenString.substring(1)), biggestSalaryMenString.length() == 1 ? 10 : biggestSalaryMen + Math.pow(10, biggestSalaryMenString.length() - 1) - Integer.parseInt(biggestSalaryMenString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red));
        //Generating the plot with the gender wage gaps
        Plot wageGapPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Gap Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution de la Différence de la Paye dans les États Unis" : "Predicția Evoluției Diferenței între Salarii în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(smallestYear, biggestYear)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestPayGapString.length() == 1 ? 10 : biggestPayGap + Math.pow(10, biggestPayGapString.length() - 1) - Integer.parseInt(biggestPayGapString.substring(1)))).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye'" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Saving the plots to be used in the graph display screen
        allGendersPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction", "png");
        allGendersPlotWithGap.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction", "png");
        womenPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-prediction", "png");
        menPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-prediction", "png");
        wageGapPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-prediction", "png");
        //Now we can generate 5 graphs - one for men's salaries, one for women's salaries, one for both, one for both that also include the wage gap, and one for the wage gap which include the generated predictions - to be displayed according to the user's choice
    }

    //Function that creates all the graphs containing specified range, that include predictions, in advance for performance boost when displaying, when the user makes different display choices
    //Uses the Plot class created by YuriY Guskov, found at https://github.com/yuriy-g/simple-java-plot
    public void createSalaryGraphWithinRangeWithPredictionsForEverybody(int minimum, int maximum) throws IOException {
        //Separating the dataset into 3 arrays (one with the years, one with men's salaries and one with women's salaries) to use in the plot
        List<Double> years = new ArrayList<>();
        List<Double> womenSalaries = new ArrayList<>();
        List<Double> menSalaries = new ArrayList<>();
        List<Double> allSalaries = new ArrayList<>();
        //Extracting the calculated pay gaps from the map into an array use in the plot
        List<Double> payGapsArray = new ArrayList<>();
        for (String[] gap : genderPayGapWithPredictions) {
            if (Integer.parseInt(gap[0]) >= minimum && Integer.parseInt(gap[0]) <= maximum)
                payGapsArray.add(Double.parseDouble(gap[1]));
        }
        for (String[] stats : datasetWithPredictions) {
            if (Integer.parseInt(stats[1]) >= minimum && Integer.parseInt(stats[1]) <= maximum) {
                if (!years.contains(Double.parseDouble(stats[1]))) years.add(Double.parseDouble(stats[1]));
                allSalaries.add(Double.parseDouble(stats[2]));
                if (stats[0].equals("Women")) womenSalaries.add(Double.parseDouble(stats[2]));
                else menSalaries.add(Double.parseDouble(stats[2]));
            }
        }
        //Determining the smallest and biggest salary to set as range for the Y axis of the plot
        int smallestSalaryEverybody = Collections.min(allSalaries).intValue();
        int biggestSalaryEverybody = Collections.max(allSalaries).intValue();
        int smallestSalaryWomen = Collections.min(womenSalaries).intValue();
        int biggestSalaryWomen = Collections.max(womenSalaries).intValue();
        int smallestSalaryMen = Collections.min(menSalaries).intValue();
        int biggestSalaryMen = Collections.max(menSalaries).intValue();
        int smallestPayGap = Collections.min(payGapsArray).intValue();
        int biggestPayGap = Collections.max(payGapsArray).intValue();
        //Transforming the above variables into string for easier manipulation later
        String smallestSalaryEverybodyString = String.valueOf(smallestSalaryEverybody);
        String biggestSalaryEverybodyString = String.valueOf(biggestSalaryEverybody);
        String smallestSalaryWomenString = String.valueOf(smallestSalaryWomen);
        String biggestSalaryWomenString = String.valueOf(biggestSalaryWomen);
        String smallestSalaryMenString = String.valueOf(smallestSalaryMen);
        String biggestSalaryMenString = String.valueOf(biggestSalaryMen);
        String smallestPayGapString = String.valueOf(smallestPayGap);
        String biggestPayGapString = String.valueOf(biggestPayGap);
        //Generating the plot with all the salaries
        Plot allGendersPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires dans les États Unis" : "Predicția Evoluției Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybodyString.length() == 1 ? 0 : smallestSalaryEverybody - Integer.parseInt(smallestSalaryEverybodyString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        Plot allGendersPlotWithGap = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage and Wage Gap Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires et de la Différence de la Paye dans les États Unis" : "Predicția Evoluției Salariilor și a Diferenței între Salarii în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryEverybody < smallestPayGap && smallestSalaryEverybodyString.length() == 1 ? 0 : smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestSalaryEverybodyString.length() == 1 ? 10 : biggestSalaryEverybody + Math.pow(10, biggestSalaryEverybodyString.length() - 1) - Integer.parseInt(biggestSalaryEverybodyString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red)).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green)).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Generating the plot with women's salaries
        Plot womenPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires dans les États Unis" : "Predicția Evoluției Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryWomenString.length() == 1 ? 0 : smallestSalaryWomen - Integer.parseInt(smallestSalaryWomenString.substring(1)), biggestSalaryWomenString.length() == 1 ? 10 : biggestSalaryWomen + Math.pow(10, biggestSalaryWomenString.length() - 1) - Integer.parseInt(biggestSalaryWomenString.substring(1)))).
                series(Main.language.equals("EN") ? "Women's wages" : Main.language.equals("FR") ? "Salaires des femmes" : "Salariile femeilor", Plot.data().xy(years, womenSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.green).color(Color.green));
        //Generating the plot with men's salaries
        Plot menPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution des Salaires dans les États Unis" : "Predicția Evoluției Salariilor în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestSalaryMenString.length() == 1 ? 0 : smallestSalaryMen - Integer.parseInt(smallestSalaryMenString.substring(1)), biggestSalaryMenString.length() == 1 ? 10 : biggestSalaryMen + Math.pow(10, biggestSalaryMenString.length() - 1) - Integer.parseInt(biggestSalaryMenString.substring(1)))).
                series(Main.language.equals("EN") ? "Men's wages" : Main.language.equals("FR") ? "Salaires d'hommes" : "Salariile bărbaților", Plot.data().xy(years, menSalaries), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.red).color(Color.red));
        //Generating the plot with the gender wage gaps
        Plot wageGapPlot = Plot.plot(Plot.plotOpts().title(Main.language.equals("EN") ? "Wage Gap Evolution Prediction in the United States" : Main.language.equals("FR") ? "Prédiction d'Évolution de la Différence de la Paye dans les États Unis" : "Predicția Evoluției Diferenței între Salarii în Statele Unite").legend(Plot.LegendFormat.BOTTOM)).
                xAxis(Main.language.equals("EN") ? "Year" : "An", Plot.axisOpts().range(minimum, maximum)).
                yAxis(Main.language.equals("EN") ? "Wage" : Main.language.equals("FR") ? "Salaire" : "Salariu", Plot.axisOpts().range(smallestPayGapString.length() == 1 ? 0 : smallestPayGap - Integer.parseInt(smallestPayGapString.substring(1)), biggestPayGapString.length() == 1 ? 10 : biggestPayGap + Math.pow(10, biggestPayGapString.length() - 1) - Integer.parseInt(biggestPayGapString.substring(1)))).
                series(Main.language.equals("EN") ? "Wage gap" : Main.language.equals("FR") ? "Différence de la paye'" : "Diferența între salarii", Plot.data().xy(years, payGapsArray), Plot.seriesOpts().marker(Plot.Marker.CIRCLE).markerColor(Color.magenta).color(Color.magenta));
        //Saving the plots to be used in the graph display screen
        allGendersPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-prediction-range", "png");
        allGendersPlotWithGap.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction-range", "png");
        womenPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/women-prediction-range", "png");
        menPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/men-prediction-range", "png");
        wageGapPlot.save("src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/wageGap-prediction-range", "png");
        //Now we can generate 5 graphs - one for men's salaries, one for women's salaries, one for both, one for both that also include the wage gap, and one for the wage gap which include the statistics within the range the user specified and the predictions - to be displayed according to the user's choice
    }

    //Function that creates a PDF containing the graph with all the genders, pay gap and predictions (if it's the case), interpretations, dataset with or without predictions and the yearly pay gaps
    public void createPDF() throws IOException, DocumentException {
        //Creating a PDF document
        Document pdf = new Document();
        PdfWriter.getInstance(pdf, new FileOutputStream("src/main/resources/com/gendergapanalyser/gendergapanalyser/Analysis.pdf"));

        //Opening it for writing
        pdf.open();

        //Creating the title of the PDF that contains the range of years it covers (1960 to the last year of the dataset or the last predicted year), centering it on the line then adding it to the PDF
        Paragraph title = new Paragraph((Main.language.equals("EN") ? "Gender Equality in the United States, since the year 1960 to " : Main.language.equals("FR") ? "Égalité entre les Genres dans les États Unis, depuis l'année 1960 jusqu'à " : "Egalitatea intre Genuri in Statele Unite, din anul 1960 pana in ") + (predictionsGenerated ? datasetWithPredictions[datasetWithPredictions.length - 1][1] : dataset[dataset.length - 1][1]), new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE));
        title.setAlignment(Element.ALIGN_CENTER);
        pdf.add(title);

        //Checking to see what the app's display mode has been set to, so that we can generate a light graph if the app is set to dark mode
        Image graph;
        if (Main.displayMode.equals("Dark")) {
            //Temporarily setting the displayMode variable to Light so we can generate light backgrounds to be set on light pages
            Main.displayMode = "Light";
            //Generating graphs based on the predictions existing or not
            if (predictionsGenerated) {
                createSalaryGraphWithPredictionsForEverybody();
            }
            else {
                createSalaryGraphForEverybody();
            }
            //Creating an image containing the graph with all the genders and the pay gap, with or without predictions
            graph = Image.getInstance(predictionsGenerated ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap.png");
            //Setting the displayMode variable back to dark
            Main.displayMode = "Dark";
            //Regenerating graphs to be dark to be displayed in the app
            if (predictionsGenerated) {
                createSalaryGraphWithPredictionsForEverybody();
            }
            else {
                createSalaryGraphForEverybody();
            }
        }
        else {
            //Creating an image containing the graph with all the genders and the pay gap, with or without predictions, scaling it so it fits the entire width of the page minus the left and right margins, and the height being the width - 150, then adding it to the PDF
            graph = Image.getInstance(predictionsGenerated ? "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap-prediction.png" : "src/main/resources/com/gendergapanalyser/gendergapanalyser/Graphs/all_genders-wageGap.png");
        }
        //Scaling the graph so it fits the entire width of the page minus the left and right margins, and the height being the width - 150, then adding it to the PDF
        graph.scaleAbsoluteWidth(pdf.getPageSize().getWidth() - pdf.leftMargin() - pdf.rightMargin());
        graph.scaleAbsoluteHeight(graph.getScaledWidth() - 150);
        pdf.add(graph);

        //Creating a paragraph as a subtitle for the women's evolution interpretation, a paragraph for the interpretation and a new line to separate it from the rest of the document, then adding them to the PDF
        //Removing diacritics if the app language is set to Romanian since itextpdf cannot write Romanian diacritics
        pdf.add(new Paragraph(Main.language.equals("EN") ? "How did women's salaries evolve?" : Main.language.equals("FR") ? "Comment ont évolué les salaires des femmes ?" : "Cum au evoluat salariile femeilor?", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLDITALIC)));
        pdf.add(new Paragraph(Main.language.equals("RO") ? Normalizer.normalize(predictionsGenerated ? womenAnalysisWithPredictions : womenAnalysis, Normalizer.Form.NFKD).replaceAll("\\p{M}", "") : predictionsGenerated ? womenAnalysisWithPredictions : womenAnalysis, new Font(Font.FontFamily.HELVETICA, 14)));
        pdf.add(new Paragraph("\n", new Font(Font.FontFamily.HELVETICA, 18)));

        //Creating a paragraph as a subtitle for the men's evolution interpretation, a paragraph for the interpretation and a new line to separate it from the rest of the document, then adding them to the PDF
        pdf.add(new Paragraph(Main.language.equals("EN") ? "How did men's salaries evolve?" : Main.language.equals("FR") ? "Comment ont évolué les salaires d'hommes ?" : "Cum au evoluat salariile barbatilor?", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLDITALIC)));
        pdf.add(new Paragraph(Main.language.equals("RO") ? Normalizer.normalize(predictionsGenerated ? menAnalysisWithPredictions : menAnalysis, Normalizer.Form.NFKD).replaceAll("\\p{M}", "") : predictionsGenerated ? menAnalysisWithPredictions : menAnalysis, new Font(Font.FontFamily.HELVETICA, 14)));
        pdf.add(new Paragraph("\n", new Font(Font.FontFamily.HELVETICA, 18)));

        //Creating a paragraph as a subtitle for the wage gap's evolution interpretation, a paragraph for the interpretation and a new line to separate it from the rest of the document, then adding them to the PDF
        pdf.add(new Paragraph(Main.language.equals("EN") ? "How did the pay gap evolve?" : Main.language.equals("FR") ? "Comment a évolué la différence de la paye ?" : "Cum a evoluat diferenta intre salarii?", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLDITALIC)));
        pdf.add(new Paragraph(Main.language.equals("RO") ? Normalizer.normalize(predictionsGenerated ? wageGapAnalysisWithPredictions : wageGapAnalysis, Normalizer.Form.NFKD).replaceAll("\\p{M}", "") : predictionsGenerated ? wageGapAnalysisWithPredictions : wageGapAnalysis, new Font(Font.FontFamily.HELVETICA, 14)));
        pdf.add(new Paragraph("\n", new Font(Font.FontFamily.HELVETICA, 18)));

        //Creating a paragraph as a subtitle for the dataset and a new line to create a bit of space between the subtitle and the dataset table, then adding them to the PDF
        pdf.add(new Paragraph(Main.language.equals("EN") ? "Data used" : Main.language.equals("FR") ? "Données utilisés" : "Date utilizate", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLDITALIC)));
        pdf.add(new Paragraph("\n", new Font(Font.FontFamily.HELVETICA, 18)));

        //Creating the table which will contain the dataset and the yearly pay gaps
        PdfPTable datasetTablePDF = new PdfPTable(4);

        //Creating 4 cells to be used as headers, setting their background color and adding them to the table
        PdfPCell yearHeader = new PdfPCell(new Phrase(Main.language.equals("EN") ? "Year" : "An"));
        yearHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        datasetTablePDF.addCell(yearHeader);
        PdfPCell womenHeader = new PdfPCell(new Phrase(Main.language.equals("EN") ? "Women's Salary" : Main.language.equals("FR") ? "Salaire des Femmes" : "Salariul Femeilor"));
        womenHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        datasetTablePDF.addCell(womenHeader);
        PdfPCell menHeader = new PdfPCell(new Phrase(Main.language.equals("EN") ? "Men's Salary" : Main.language.equals("FR") ? "Salaire d'Hommes" : "Salariul Barbatilor"));
        menHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        datasetTablePDF.addCell(menHeader);
        PdfPCell gapHeader = new PdfPCell(new Phrase(Main.language.equals("EN") ? "Pay Gap" : Main.language.equals("FR") ? "Différence de la Paye" : "Diferenta intre Salarii"));
        gapHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        datasetTablePDF.addCell(gapHeader);

        //Creating 4 variables where each year and the respective salaries and wage gap will be stored
        String year = "";
        String womanSalary = "";
        String manSalary = "";
        String payGap = "";

        //Iterating over the dataset that includes or not the predictions, depending on the case
        for (String[] statistic : predictionsGenerated ? datasetWithPredictions : dataset) {
            //If we have something set in the 4 string variables above
            if (!year.equals("") && !womanSalary.equals("") && !manSalary.equals("") && !payGap.equals("")) {
                //We create a cell for each, containing what each string variable contains
                PdfPCell yearCell = new PdfPCell(new Phrase(year));
                //We only set the year's cell background to be different
                yearCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                PdfPCell womanSalaryCell = new PdfPCell(new Phrase(womanSalary));
                PdfPCell manSalaryCell = new PdfPCell(new Phrase(manSalary));
                PdfPCell payGapCell = new PdfPCell(new Phrase(payGap));

                //We add the cells to the table
                datasetTablePDF.addCell(yearCell);
                datasetTablePDF.addCell(womanSalaryCell);
                datasetTablePDF.addCell(manSalaryCell);
                datasetTablePDF.addCell(payGapCell);

                //Then we set the string variables with the current statistics
                year = statistic[1];
                if (statistic[0].equals("Women"))
                    womanSalary = formatSalary(statistic[2]);
                else
                    womanSalary = "";
                if (statistic[0].equals("Men"))
                    manSalary = formatSalary(statistic[2]);
                else
                    manSalary = "";
                //Here we subtract the first year of the array from the current year because we need the positional indexes, 1960 is the first year of the dataset
                payGap = formatSalary(predictionsGenerated ? genderPayGapWithPredictions[Integer.parseInt(year) - Integer.parseInt(genderPayGapWithPredictions[0][0])][1] : genderPayGap[Integer.parseInt(year) - Integer.parseInt(genderPayGap[0][0])][1]);
            }
            //If we have just begun creating the table and don't have something in the 4 string variables, we set them with the current statistic
            else {
                year = statistic[1];
                if (statistic[0].equals("Women"))
                    womanSalary = formatSalary(statistic[2]);
                else
                    manSalary = formatSalary(statistic[2]);
                payGap = formatSalary(predictionsGenerated ? genderPayGapWithPredictions[Integer.parseInt(year) - Integer.parseInt(genderPayGapWithPredictions[0][0])][1] : genderPayGap[Integer.parseInt(year) - Integer.parseInt(genderPayGap[0][0])][1]);
            }
        }

        //Because iterating the dataset finishes before adding the last year to the PDF table, we add it now
        //We create a cell for each, containing what each string variable contains
        PdfPCell yearCell = new PdfPCell(new Phrase(year));
        //We only set the year's cell background to be different
        yearCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        PdfPCell womanSalaryCell = new PdfPCell(new Phrase(womanSalary));
        PdfPCell manSalaryCell = new PdfPCell(new Phrase(manSalary));
        PdfPCell payGapCell = new PdfPCell(new Phrase(payGap));

        //We add the cells to the table
        datasetTablePDF.addCell(yearCell);
        datasetTablePDF.addCell(womanSalaryCell);
        datasetTablePDF.addCell(manSalaryCell);
        datasetTablePDF.addCell(payGapCell);

        //We add the created table to the PDF
        pdf.add(datasetTablePDF);

        //We finish editing the PDF
        pdf.close();

        PDFGeneratedWithPredictions = predictionsGenerated;
        changedLanguage = false;
    }
}
