
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.util.LineReader;


import l2java.l2hanaterm;
import l2java.l2syno;
import l2java.TXtTOKEN;

public class Distill {

    public static class MapClass extends
            Mapper<MD5Hash, Document, MD5Hash, Carrier> {

        private Set<Text> rankdown = new HashSet<Text>();
        private Set<String> stopwords = new HashSet<String>();

        // L2 option
        private l2hanaterm grdyIndexer;
        private l2hanaterm atomicIndexer;
        private l2hanaterm sgmtIndexer;
        private l2hanaterm sgmtSimpleIndexer;
        private l2hanaterm qrySgmtIndexer;
        private l2hanaterm qryGrdyIndexer;

        private l2syno syno;

        // Counters
        private Counter dupTermCount;
        private Counter rankdownCount;
        private Counter stopwordsCount;

        private Carrier carrier = new Carrier();
        private Text categoryItem[] = new Text[JShopping.MAX_CATEGORY_DEPTH];
        private Text categoryId[] = new Text[JShopping.MAX_CATEGORY_DEPTH];
        private Text categoryName[] = new Text[JShopping.MAX_CATEGORY_DEPTH];

        private Set<String> dupCheck = new HashSet<String>();

        //
        private Map<String, String> refineMultiCategory = new HashMap<String, String>();
        private Text pricePoints = new Text();

        private void pushTermToCarrier(String term, Enum<?> type,
                boolean dupChecking) {
            if (stopwords.contains(term)) {
                stopwordsCount.increment(1);
                return;
            }

            if (dupCheck.contains(term) && dupChecking == true) {
                dupTermCount.increment(1);
            } else {
                dupCheck.add(term);

                int ret = syno.lookupOne(term);
                if (ret == 1) {
                    String synoStr = syno.getSynonym();
                    carrier.push(type, "##" + synoStr + "\t" + term);
                } else {
                    carrier.push(type, term);
                }
            }
        }

        private String execTokenIndexer(l2hanaterm indexer, Enum<?> type,
                String input, boolean dupChecking) {
            StringBuffer tokensWithSeparator = new StringBuffer();
            int cnt = indexer.execIndexerToList(input, 1024);
            for (int i = 0; i < cnt; i++) {
                TXtTOKEN token = indexer.getListToken(i);
                String term = token.getTerm();
                pushTermToCarrier(term, type, dupChecking);
                tokensWithSeparator.append(term);
                if (i < cnt - 1)
                    tokensWithSeparator.append(JShopping.TermSeparator);
            }

            return tokensWithSeparator.toString();
        }

        private void execTermIndexer(l2hanaterm indexer, Enum<?> type,
                String input, boolean isCommaSplit, boolean dupChecking) {
            dupCheck.clear();

            if (isCommaSplit) {
                StringTokenizer tokenizer = new StringTokenizer(input, ",");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    execTokenIndexer(indexer, type, token, dupChecking);
                }
            } else {
                execTokenIndexer(indexer, type, input, dupChecking);
            }
        }

        private void execTermPairIndexer(l2hanaterm firstIndexer,
                Enum<?> firstType, l2hanaterm secondIndexer,
                Enum<?> secondType, Enum<?> pairType, String input) {
            // TxPair로 이루어지는 색인어 텀은 FMP계산에는 사용되지 않아야 한다.
            // FMP 계산을 위해서는 중복 체크가 되어야 하나,
            // TxPair에서는 구분자(쉼표)로 나뉜 경우마다 중복 체크가 되어 같은 term이 2번 나오게 될 수 있는 점에
            // 유의한다.
            StringTokenizer tokenizer = new StringTokenizer(input, ",");
            StringBuffer txPair = new StringBuffer();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                dupCheck.clear();
                txPair.append(execTokenIndexer(firstIndexer, firstType, token,
                        true));
                txPair.append(JShopping.IndexerSeparator);
                dupCheck.clear();
                txPair.append(execTokenIndexer(secondIndexer, secondType,
                        token, true));
                carrier.push(pairType, txPair.toString());
                txPair.setLength(0);
            }

        }

        private void execTermIndexer(l2hanaterm indexer, Enum<?> type,
                String input, boolean dupChecking) {
            execTermIndexer(indexer, type, input, false, dupChecking);
        }

        private void emitCategory(int idx, Text name, Text id) {
            Enum<DtItem> eN;
            Enum<DtItem> eI;
            Enum<DtItem> eA;
            Enum<DtItem> eQ;
            Enum<DtItem> eSgmt;
            Enum<DtItem> eQgdy;
            Enum<DtItem> ePair;
            switch (idx) {
            case 0:
                eN = DtItem.FMP_CATEGORY_1_NAME;
                eI = DtItem.FMP_CATEGORY_1_ID;
                eA = DtItem.TX_CATEGORY_1_ATOMIC;
                eQ = DtItem.TX_CATEGORY_1_QRYSGMT;
                eSgmt = DtItem.TX_CATEGORY_1_SGMT;
                eQgdy = DtItem.TX_CATEGORY_1_QRYGRDY;
                ePair = DtItem.TX_CATEGORY_1_TXPAIR;
                break;
            case 1:
                eN = DtItem.FMP_CATEGORY_2_NAME;
                eI = DtItem.FMP_CATEGORY_2_ID;
                eA = DtItem.TX_CATEGORY_2_ATOMIC;
                eQ = DtItem.TX_CATEGORY_2_QRYSGMT;
                eSgmt = DtItem.TX_CATEGORY_2_SGMT;
                eQgdy = DtItem.TX_CATEGORY_2_QRYGRDY;
                ePair = DtItem.TX_CATEGORY_2_TXPAIR;
                break;
            case 2:
                eN = DtItem.FMP_CATEGORY_3_NAME;
                eI = DtItem.FMP_CATEGORY_3_ID;
                eA = DtItem.TX_CATEGORY_3_ATOMIC;
                eQ = DtItem.TX_CATEGORY_3_QRYSGMT;
                eSgmt = DtItem.TX_CATEGORY_3_SGMT;
                eQgdy = DtItem.TX_CATEGORY_3_QRYGRDY;
                ePair = DtItem.TX_CATEGORY_3_TXPAIR;
                break;
            case 3:
                eN = DtItem.FMP_CATEGORY_4_NAME;
                eI = DtItem.FMP_CATEGORY_4_ID;
                eA = DtItem.TX_CATEGORY_4_ATOMIC;
                eQ = DtItem.TX_CATEGORY_4_QRYSGMT;
                eSgmt = DtItem.TX_CATEGORY_4_SGMT;
                eQgdy = DtItem.TX_CATEGORY_4_QRYGRDY;
                ePair = DtItem.TX_CATEGORY_4_TXPAIR;
                break;
            case 4:
                eN = DtItem.FMP_CATEGORY_5_NAME;
                eI = DtItem.FMP_CATEGORY_5_ID;
                eA = DtItem.TX_CATEGORY_5_ATOMIC;
                eQ = DtItem.TX_CATEGORY_5_QRYSGMT;
                eSgmt = DtItem.TX_CATEGORY_5_SGMT;
                eQgdy = DtItem.TX_CATEGORY_5_QRYGRDY;
                ePair = DtItem.TX_CATEGORY_5_TXPAIR;
                break;
            case 5:
                eN = DtItem.FMP_CATEGORY_6_NAME;
                eI = DtItem.FMP_CATEGORY_6_ID;
                eA = DtItem.TX_CATEGORY_6_ATOMIC;
                eQ = DtItem.TX_CATEGORY_6_QRYSGMT;
                eSgmt = DtItem.TX_CATEGORY_6_SGMT;
                eQgdy = DtItem.TX_CATEGORY_6_QRYGRDY;
                ePair = DtItem.TX_CATEGORY_6_TXPAIR;
                break;
            default:
                return;
            }
            carrier.push(eN, name);
            carrier.push(eI, id);

            String str = name.toString();
            execTermIndexer(atomicIndexer, eA, str, true, true);
            execTermIndexer(qrySgmtIndexer, eQ, str, false, true);
            // execTermIndexer(sgmtIndexer, eSgmt, str, true, true);
            // execTermIndexer(qryGrdyIndexer, eQgdy, str, true, true);
            execTermPairIndexer(sgmtIndexer, eSgmt, qryGrdyIndexer, eQgdy,
                    ePair, str);
        }

        private String replaceString(String category) {
            String replacedCategory = category.trim();
            replacedCategory = replacedCategory.replace(" ", "");
            replacedCategory = replacedCategory.replace("\t", ">");
            replacedCategory = replacedCategory.replace("、", ",");
            replacedCategory = replacedCategory.replace("/", ">");
            replacedCategory = replacedCategory.replace("・", ",");

            return replacedCategory;
        }

        private void processCategoryId(String itemCategory) {
            int i;
            for (i = 0; i < JShopping.MAX_CATEGORY_DEPTH; i++) {
                categoryId[i].clear();
            }

            String[] st = itemCategory.split(">");
            int depthCount = Math.min(st.length, JShopping.MAX_CATEGORY_DEPTH);
            for (i = 0; i < depthCount; i++) {
                if (i == 0)
                    categoryId[i].set(st[i]);
                else {
                    String catId = categoryId[i - 1] + ">" + st[i];
                    categoryId[i].set(catId);
                }

                if (i == JShopping.MAX_CATEGORY_DEPTH)
                    break;
            }
        }

        private void processCategoryName(String itemCategory) {
            int i;
            for (i = 0; i < JShopping.MAX_CATEGORY_DEPTH; i++) {
                categoryName[i].clear();
            }

            String[] st = itemCategory.split(">");
            int depthCount = Math.min(st.length, JShopping.MAX_CATEGORY_DEPTH);
            for (i = 0; i < depthCount; i++) {
                categoryName[i].set(st[i]);

                if (i == JShopping.MAX_CATEGORY_DEPTH)
                    break;
            }
        }

        private void autoRefineCategoryName(String itemName) {
            int i;

            for (i = 1; i < JShopping.MAX_CATEGORY_DEPTH; i++) {
                String catName = categoryName[i - 1].toString();
                // category의 토큰이 자식카테고리의 서브스트링이면 부모Category를 삭제한다.
                if (i < 6) {
                    String[] tokens = catName.split(",");
                    for (int j = 0; j < tokens.length; j++) {
                        String cat = categoryName[i].toString();
                        if (cat.contains(tokens[j]) == true)
                            categoryName[i - 1].set("");
                    }
                }

                catName = categoryName[i - 1].toString();
                // category의 자식카테고리가 "その他" 이고 multi의 토큰이 itemName에 없으면 삭제한다.
                // leafCategory가 multi이고 multi의 토큰이 itemName에 없으면 삭제한다.
                if (catName.contains(",") == true) {
                    if (categoryName[i].toString().compareTo("その他") == 0
                            || categoryName[i].toString().isEmpty() == true) {
                        StringBuffer refCat = new StringBuffer();
                        String[] tokens = catName.split(",");
                        boolean isAdded = false;
                        for (int j = 0; j < tokens.length; j++) {
                            if (itemName.contains(tokens[j]) == true) {
                                if (isAdded == true)
                                    refCat.append(",");
                                refCat.append(tokens[j]);
                                isAdded = true;
                            }
                        }
                        if (isAdded == true)
                            categoryName[i - 1].set(refCat.toString());
                    }
                }
            }

            // leafCategory가 multi이고 multi의 토큰이 itemName에 없으면 삭제한다.
            String catName = categoryName[JShopping.MAX_CATEGORY_DEPTH - 1]
                    .toString();
            if (catName.contains(",") == true) {
                StringBuffer refCat = new StringBuffer();
                String[] tokens = catName.split(",");
                boolean isAdded = false;
                for (int j = 0; j < tokens.length; j++) {
                    if (itemName.contains(tokens[j]) == true) {
                        if (isAdded == true)
                            refCat.append(",");
                        refCat.append(tokens[j]);
                        isAdded = true;
                    }
                }
                if (isAdded == true)
                    categoryName[JShopping.MAX_CATEGORY_DEPTH - 1].set(refCat
                            .toString());
            }
        }

        private l2hanaterm makeIndexer(String indexerName, String stopwords, String l2dicPath, 
                                       String options) throws IOException {
          l2hanaterm indexer = new l2hanaterm();
          int ret = indexer.initIndexer(indexerName, stopwords, l2dicPath + " " + options);

          if (ret < 0) {
            System.out.println("Error Code : " + ret); 
            throw new IOException(indexerName + " index initialize fail");
          }
          return indexer;
        }

        @Override
        public void setup(Context context) throws IOException {
            System.loadLibrary("l2java");

            String l2DicPath = context.getConfiguration().get(
                    "l2.dictionary.path");
            System.out.println("DicPath : " + l2DicPath);

            if (l2hanaterm.Initialize() != 0) {
                throw new IOException("initialize fail...");
            }

            grdyIndexer = makeIndexer("grdy", "stopwords", l2DicPath, IdxOpt.ITEM_NAME_GRDY.TAG);
            atomicIndexer = makeIndexer("atomic", "stopwords", l2DicPath, IdxOpt.CATEGORY_ATOMIC.TAG);
            sgmtIndexer = makeIndexer("sgmt", "stopwords", l2DicPath, IdxOpt.CATEGORY_SGMT.TAG);
            sgmtSimpleIndexer = makeIndexer("sgmt", "stopwords", l2DicPath, IdxOpt.ITEM_NAME_SGMT_SIMPLE.TAG);
            qrySgmtIndexer = makeIndexer("qrysgmt", "stopwords", l2DicPath, IdxOpt.CATEGORY_QRYSGMT.TAG);
            qryGrdyIndexer = makeIndexer("qrygrdy", "stopwords", l2DicPath, IdxOpt.CATEGORY_QRYGRDY.TAG);

            syno = new l2syno();
            if (syno.open(l2DicPath, "jshopping", "") != 0) {
                throw new IOException("syno open fail");
            }

            dupTermCount = context.getCounter("termx", "dup term");
            rankdownCount = context.getCounter("doc", "rankdown");
            stopwordsCount = context.getCounter("termx", "stopwords");

            for (int i = 0; i < JShopping.MAX_CATEGORY_DEPTH; i++) {
                categoryItem[i] = new Text();
                categoryName[i] = new Text();
                categoryId[i] = new Text();
            }

            new FileReader() {
              @Override
              public void process(Text line) {
                rankdown.add(new Text(line));
              }
            }.read("rankdown");

            new FileReader() {
              @Override
              public void process(Text line) {
                stopwords.add(line.toString());
              }
            }.read("stopwords");

            new FileReader() {
              @Override
              public void process(Text line) {
                String[] tokens = line.toString().split("\\|\\|\t");
             	  String keyCategory = replaceString(tokens[0]);
             	  String valueCategory = replaceString(tokens[1]);
             	  refineMultiCategory.put(keyCategory, valueCategory);
              }
            }.read("refined_multi_category");
        }

        @Override
        public void map(MD5Hash key, Document value, Context context)
                throws IOException, InterruptedException {
            carrier.clear();

            int salesRank = JShopping.MAX_SALERANK;
            int compareCount = 1;
            int price = 0;
            int isAdultProduct = 0;
            boolean isRankdown = false;
            boolean isCategoryEmpty = true;
            String itemImageUrl = "";
            pricePoints.set("0");

            for (int i = 0; i < JShopping.MAX_CATEGORY_DEPTH; i++) {
                categoryName[i].clear();
                categoryId[i].clear();
            }

            for (ItemKeyValue item : value) {
                Text ik = item.getKey();
                if (ik.equals(Item.ITEM_UNIQUE_ID.VALUE)) {
                    // carrier.push(DtItem.ITEM_UNIQUE_ID, item.getValue());
                    if (rankdown.contains(item.getValue())) {
                        isRankdown = true;
                    }
                } else if (ik.equals(Item.ITEM_CATEGORY.VALUE)) {
                    isCategoryEmpty = false;
                    StringTokenizer st = new StringTokenizer(item.getValue()
                            .toString(), "\t");
                    String replacedCategory = replaceString(st.nextToken());
                    String refinedCategory = refineMultiCategory
                            .get(replacedCategory);
                    if (refinedCategory == null)
                        refinedCategory = replacedCategory;

                    processCategoryName(refinedCategory);
                    processCategoryId(replacedCategory);

                    carrier.push(DtItem.FMP_FULL_CATEGORY, refinedCategory);
                    // carrier.push(DtItem.ITEM_CATEGORY, item.getValue());
                } else if (ik.equals(Item.ITEM_SALESRANK.VALUE)) {
                    salesRank = Integer.parseInt(item.getValue().toString());
                } else if (ik.equals(Item.ITEM_COMPARE_COUNT.VALUE)) {
                    compareCount = Integer.parseInt(item.getValue().toString());
                } else if (ik.equals(Item.ITEM_NAME.VALUE)) {
                    String str = item.getValue().toString();
                    execTermIndexer(grdyIndexer, DtItem.TX_ITEM_NAME_GRDY, str,
                            true);
                    execTermIndexer(sgmtIndexer, DtItem.TX_ITEM_NAME_SGMT, str,
                            true);
                    execTermIndexer(sgmtSimpleIndexer,
                            DtItem.TX_ITEM_NAME_SGMT_SIMPLE, str, false);

                    // fmpCategoryName 정제
                    autoRefineCategoryName(str);
                } else if (ik.equals(Item.ITEM_BRAND.VALUE)) {
                    String str = item.getValue().toString();
                    execTermIndexer(qrySgmtIndexer,
                            DtItem.TX_ITEM_BRAND_QRYSGMT, str, true);
                    execTermIndexer(qryGrdyIndexer,
                            DtItem.TX_ITEM_BRAND_QRYGRDY, str, true);
                    // carrier.push(DtItem.ITEM_BRAND, item.getValue());
                } else if (ik.equals(Item.ITEM_MANUFACTURER.VALUE)) {
                    String str = item.getValue().toString();
                    execTermIndexer(qrySgmtIndexer,
                            DtItem.TX_ITEM_MANUFACTURER_QRYSGMT, str, true);
                    execTermIndexer(qryGrdyIndexer,
                            DtItem.TX_ITEM_MANUFACTURER_QRYGRDY, str, true);
                    // carrier.push(DtItem.ITEM_MANUFACTURER, item.getValue());
                } else if (ik.equals(Item.ITEM_MERCHANT_NAME.VALUE)) {
                    String str = item.getValue().toString();
                    execTermIndexer(qrySgmtIndexer,
                            DtItem.TX_ITEM_MERCHANT_NAME_QRYSGMT, str, true);
                    execTermIndexer(qryGrdyIndexer,
                            DtItem.TX_ITEM_MERCHANT_NAME_QRYGRDY, str, true);
                    // carrier.push(DtItem.ITEM_MERCHANT_NAME, item.getValue());
                } else if (ik.equals(Item.PRICE.VALUE)) {
                    // Price 필드에 숫자 이외의 문자를 제거
                    String strPrice = item.getValue().toString();
                    if (!strPrice.equals(null)) {
                        try {
                            price = Integer.parseInt(strPrice);
                        } catch (NumberFormatException nfe) {
                            strPrice = strPrice.replaceAll("[A-Za-z]", "")
                                    .trim();
                            price = Integer.parseInt(strPrice);
                        }
                    }
                } else if (ik.equals(Item.IS_ADULT_PRODUCT.VALUE)) {
                    String strIsAdultProduct = item.getValue().toString();
                    if (strIsAdultProduct.equals(null))
                        strIsAdultProduct = "0";
                    isAdultProduct = Integer.parseInt(strIsAdultProduct);
                } else if (ik.equals(Item.ITEM_TYPE.VALUE)) {
                    // ITEM TYPE 출력
                    carrier.push(DtItem.ITEM_TYPE, item.getValue());
                    // ITEM TYPE이 2 이면 IsYahoo = 1
                    if (item.getValue().toString().equals("2") == true)
                        carrier.push(DtItem.IS_YAHOO, "1");
                    else
                        carrier.push(DtItem.IS_YAHOO, "0");
                } else if (ik.equals(Item.ITEM_PRICE_POINT.VALUE)) {
                    String[] points = item.getValue().toString().split("\\:");

                    if (points.length == 3) {
                        pricePoints.set(points[1]);
                    } else
                        pricePoints.set(item.getValue());
                } else if (ik.equals(Item.ITEM_IMAGE_URL.VALUE)) {
                    itemImageUrl = item.getValue().toString();
                    itemImageUrl = itemImageUrl.replace(
                            "http://item.shopping.c.yimg.jp/i/l/",
                            "http://item.shopping.c.yimg.jp/i/e/");
                }

                for (int i = 0; i < DtItem.values().length; i++) {
                    if (DtItem.values()[i].VALUE.equals(ik)) {
                        carrier.push(DtItem.values()[i], item.getValue());
                    }
                }
            }

            for (int i = 0; i < JShopping.MAX_CATEGORY_DEPTH; i++)
                emitCategory(i, categoryName[i], categoryId[i]);

            double salesRankScore = (double) (Math.max(0,
                    JShopping.MAX_SALERANK - salesRank))
                    / JShopping.MAX_SALERANK;
            double compareCountScore = Math.log(Math.min(JShopping.MAX_CPCNT,
                    compareCount)) / Math.log(JShopping.MAX_CPCNT);

            carrier.push(DtItem.ITEM_SALESRANK_SCORE, 
                         String.valueOf(salesRankScore));

            carrier.push(DtItem.ITEM_COMPARE_COUNT_SCORE, 
                         String.valueOf(compareCountScore));

            carrier.push(DtItem.PRICE, String.valueOf(price));
            carrier.push(DtItem.ITEM_PRICE_POINT, pricePoints);
            carrier.push(DtItem.ITEM_IMAGE_URL, itemImageUrl);

            if (isRankdown || isCategoryEmpty == true) {
                carrier.push(DtItem.RANK_DOWN, JShopping.rankdownT);
                rankdownCount.increment(1);
            } else {
                carrier.push(DtItem.RANK_DOWN, JShopping.rankdownF);
            }

            carrier.push(DtItem.IS_ADULT_PRODUCT, 
                         String.valueOf(isAdultProduct));

            context.write(key, carrier);
        }

        @Override
        public void cleanup(Context context) throws IOException,
                InterruptedException {

            grdyIndexer.closeIndexer();
            atomicIndexer.closeIndexer();
            sgmtIndexer.closeIndexer();
            sgmtSimpleIndexer.closeIndexer();
            qrySgmtIndexer.closeIndexer();
            qryGrdyIndexer.closeIndexer();

            syno.close();
        }
    }
}
