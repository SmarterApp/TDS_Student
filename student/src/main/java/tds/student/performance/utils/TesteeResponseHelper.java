package tds.student.performance.utils;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import tds.student.performance.domain.InsertTesteeResponse;
import tds.student.performance.domain.ItemForTesteeResponse;

import java.util.List;

public class TesteeResponseHelper {


    // Filter the  ItemForTesteeResponse List by itemKeys.  Transform the filtered list to InsertTesteeResponse
    public static List<InsertTesteeResponse> createInsertsList(List<ItemForTesteeResponse> itemInsertList, String itemKeys, Character delimiter) {

        final List<String> itemKeysIterable = Splitter.on(delimiter).omitEmptyStrings().trimResults().splitToList(itemKeys);

        Predicate<ItemForTesteeResponse> predicate = new Predicate<ItemForTesteeResponse>() {
            @Override
            public boolean apply(ItemForTesteeResponse input) {
                return input != null && itemKeysIterable.contains(input.getBankItemKey());
            }
        };

        Function<ItemForTesteeResponse, InsertTesteeResponse> transform = new Function<ItemForTesteeResponse, InsertTesteeResponse>(){
            @Override
            public InsertTesteeResponse apply(ItemForTesteeResponse input) {
                return new InsertTesteeResponse(input);
            }
        };

        return FluentIterable
                .from(itemInsertList)
                .filter(predicate)
                .transform(transform)
                .toList();

    }

    // Filter the input list for null form position.
    public static List<InsertTesteeResponse> nullFormPositionList(List<InsertTesteeResponse> itemList) {
        return FluentIterable
                .from(itemList)
                .filter(new Predicate<InsertTesteeResponse>() {
                    @Override
                    public boolean apply(InsertTesteeResponse input) {
                        return input != null && input.getFormPosition() == null;
                    }
                })
                .toList();
    }

    // Sort the input list by form position.
    // Use the first as the form start.
    // If first then update relative position for items in the list.
    // todo: emulating existing logic, but why not return the sorted list.
    public static void updateItemPosition(List<InsertTesteeResponse> itemList) {

        InsertTesteeResponse firstTestee = Iterables.getFirst(
                Ordering.natural()
                        .nullsLast()
                        .onResultOf(new Function<InsertTesteeResponse, Integer>() {
                            public Integer apply(InsertTesteeResponse item) {
                                return item.getFormPosition();
                            }
                        }).sortedCopy(itemList)
                , null);

        if ( firstTestee != null && firstTestee.getFormPosition() != null ) {
            Integer formStart = firstTestee.getFormPosition();

           for ( InsertTesteeResponse i : itemList ) {
               i.setItemPosition( i.getItemPosition() - formStart );
           }
        }
    }

    //  String SQL_QUERY10 = "select  bankitemkey from ${insertsTableName} where relativePosition is null limit 1";
    //  String SQL_QUERY9  = "select relativePosition from ${insertsTableName} group by relativePosition having count(*) > 1";
    //
    public static Boolean ambiguousItemPosition(List<InsertTesteeResponse> itemList) {

        // A null item position is Ambiguous
        for ( InsertTesteeResponse i : itemList) {
            if ( i.getItemPosition() == null ) {
                return true;
            }
        }
        // Check for NOT strict order (ie. no duplicate item position).
        return !Ordering.natural()
                .nullsLast()
                .onResultOf(new Function<InsertTesteeResponse, Integer>() {
                    public Integer apply(InsertTesteeResponse item) {
                        return item.getItemPosition();
                    }
                }).isStrictlyOrdered(itemList);
    }

    // String SQL_QUERY11 = "select  bankitemkey from ${insertsTableName} where position is null limit 1";
    // String SQL_QUERY12 = "select min(relativePosition) as minpos from ${insertsTableName} where position is null;";
    // String SQL_UPDATE2 = "update ${insertsTableName} set position = ${lastpos} + 1 where relativePosition = ${minpos};";
    // Set The actual position based on the itemPosition ( form relative ) and the last position if there was a previous form.
    public static List<InsertTesteeResponse> incrementItemPositionByLast(final List<InsertTesteeResponse> itemList, final Integer lastPosition) {

        int last = lastPosition;

        List<InsertTesteeResponse> sorted = Ordering.natural()
                .nullsLast()
                .onResultOf(new Function<InsertTesteeResponse, Integer>() {
                    public Integer apply(InsertTesteeResponse item) {
                        return item.getItemPosition();
                    }
                }).immutableSortedCopy(itemList);

        for ( InsertTesteeResponse i : sorted ) {
            i.setPosition(++last);
        }

        return sorted;
    }

}
