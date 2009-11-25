/* CPSRecord.java - Created: December 8, 2007
 * Copyright (C) 2007, 2008 Clayton Carter
 * 
 * This file is part of the project "Crop Planning Software".  For more
 * information:
 *    website: http://cropplanning.googlecode.com
 *    email:   cropplanning@gmail.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package CPS.Data;

import CPS.Data.CPSDatum.CPSDatumState;
import CPS.Module.CPSDataModelConstants;
import CPS.Module.CPSModule;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import net.sf.persist.annotations.Column;

/**
 * This is an abstract class to define the format used by all "record" data structures
 * such as CPSCrop (holding data about crop entries) and CPSPlanting (holding
 * data about individual crop plan plantings).
 */
public abstract class CPSRecord {
   
   public static final int PROP_ID = CPSDataModelConstants.PROP_CROP_ID;
   public static final int PROP_COMMON_ID = CPSDataModelConstants.PROP_COMMON_ID;
   
   private boolean representsMultiIDs;
   protected CPSDatum<Integer> recordID;
   protected CPSDatum<ArrayList<Integer>> commonIDs;
   
   protected abstract int lastValidProperty();
   protected ArrayList<Integer> changedProps = new ArrayList<Integer>();

   private boolean useRawOutput = false;
   
   public CPSDatum getDatum( int prop ) {

       switch ( prop ) {
           case PROP_ID:        return recordID;
           case PROP_COMMON_ID: return commonIDs;
           default:             return null;
       }
       
   }


   @Override
   public abstract String toString();
   public abstract void finishUp();

   /**
    * Forces recalculation of data values which depend upon a certain property.
    * @param propNum The property number whose dependent values will be
    *        recalcuated, or -1 to recalculate all calculable values.
    */
   protected abstract void updateCalculations( int propNum );


   protected void debug( String message ) {
      CPSModule.debug( this.getClass().getName(), message );
   }

   @Column( primaryKey=true, autoGenerated=true )
   public int getID() { 
       if ( doesRepresentMultipleRecords() )
          return recordID.getBlankValue();
       else
         return recordID.getValueAsInt();
   }
   public void setID( int i ) {
       if ( ! doesRepresentMultipleRecords() )
          set( recordID, new Integer( i )); 
    } 
   
   public boolean isSingleRecord() {
       return ! ( getCommonIDs().size() > 0 );
   }
   
   /**
    * Retrieves the lists of IDs that this record represents.  If this record only represents a single
    * record, then this will return an empty list, or whatever is the default value of the commonIDs field.
    * @return An List of Integers, each of which is a record ID represented by this object.  If
    *         this object only represents a single ID, this list will be empty and method getID() 
    *         should be used.
    * @see getID()
    */
   public ArrayList<Integer> getCommonIDs() {
      if ( doesRepresentMultipleRecords() )
         return commonIDs.getValue();
      else
         return commonIDs.getBlankValue();
   }
   /**
    * Record the list of ids which this record represents and sets this record into multiple ID mode.
    * @param ids A ArrayList of Integers which this record represents.
    */
   public void setCommonIDs( List<Integer> ids ) {
      setRepresentsMultipleRecords();
      if ( ids instanceof ArrayList )
           set( commonIDs, (ArrayList<Integer>) ids );
      else {
          set( commonIDs, new ArrayList<Integer>( ids ) );
      }
   }
    
   /**
    * Sets this object to represent a single record.
    */
   public void setRepresentsSingleRecord() {
      representsMultiIDs = false;
   }
   /**
    * Sets this object to represents multiple records.
    */
   public void setRepresentsMultipleRecords() {
      representsMultiIDs = true;
   }
   /**
    * @return true if this object represents multiple records, false if it represents only one.
    */
   public boolean doesRepresentMultipleRecords() {
      return representsMultiIDs;
   }

   public void useRawOutput( boolean b ) {
       useRawOutput = b;
   }

   public boolean useRawOutput() {
       return useRawOutput;
   }

   public Integer getInt( int prop ) {
      if ( useRawOutput() )
         return (Integer) getDatum( prop ).getValue( useRawOutput() );
      else
         return getDatum( prop ).getValueAsInt();
//      Integer i = get( prop );
//      return i.intValue();
   }
   public String formatInt( int i ) {
      if ( i == -1 )
         return "";
      else 
         return "" + i;
   }
   public CPSBoolean getBoolean( int prop ) {
      if ( useRawOutput() )
         return (CPSBoolean) getDatum( prop ).getValue( useRawOutput() );
      else
         return new CPSBoolean( getDatum( prop ).getValueAsBoolean() );
//      Boolean b = get( prop );
//      return b.booleanValue();
   }
   public Float getFloat( int prop ) {
      if ( useRawOutput() )
         return (Float) getDatum( prop ).getValue( useRawOutput() );
      else
         return getDatum( prop ).getValueAsFloat();
//      Float f = get( prop );
//      return f.floatValue();
   }
   public String formatFloat( float f ) {
      return formatFloat( f, -1 );
   }
   public static String formatFloat( float n, int precision ) {
      
       if      ( n == -1f )
           return "";
       else if ( precision < 1 )
           return "" + n;
       else {
          // prepare our multiplcation factor
          double p = Math.pow( 10, precision );
          // if there is NO fractional part
          if ( Math.floor( n ) == n )
             return "" + (int) n;
          else
             return "" + (float) ( (int) ( n * p ) ) / p;
       }
       
   }

    /**
     * Property retrieval abstraction method.
     *
     * @param prop Property number to retrieve
     */
   public <T> T get( int prop ) {
//      debug( "Getting value of " + getDatum(prop).getName() + " as " + getDatum( prop ).getValue( useRawOutput() ));
      return (T) getDatum( prop ).getValue( useRawOutput() );
   }
   
   public CPSDatumState getStateOf( int prop ) {
      return getDatum( prop ).getState();
   }
   
   private boolean isNull( CPSDatum d ) {
      boolean b = false;
      b |= d == null;
      b |= isObjectNull(d.getValue());
      return b;
   }
   
   private boolean isObjectNull( Object o ) {
      boolean b = false;
      b |= o == null;
      b |= o instanceof String && ((String) o).equalsIgnoreCase( "null" );
      b |= o instanceof Integer && ((Integer) o).intValue() == -1;
      b |= o instanceof Float && ((Float) o).floatValue() == -1.0;
      b |= ( o instanceof java.util.Date || o instanceof java.sql.Date ) && 
           ((java.util.Date) o).getTime() == 0;
      return b;
   }
   
   public abstract CPSRecord diff( CPSRecord comparedTo );
   public CPSRecord diff( CPSRecord thatRecord, CPSRecord diffs ) {
       
       debug( "Calculating difference between:\n" +
               this.toString() + "\n" +
               thatRecord.toString() );
       
       boolean diffsExists = false;
       
       Iterator<CPSDatum> thisIt = this.iterator();
       Iterator<CPSDatum> thatIt = thatRecord.iterator();
       Iterator<CPSDatum> deltIt = diffs.iterator();
       
       CPSDatum thi, that;
       while ( thisIt.hasNext() && thatIt.hasNext() && deltIt.hasNext() ) {
          thi = thisIt.next();
          that = thatIt.next();
//          delta = deltIt.next();
          
          /*
           * if this is CALCULATED and that IS NOT VALID, then skip
           * if this IS NOT valid AND that IS valid OR   (means: new info added)
           * if this IS     valid AND that IS valid AND
           * if this IS NOT equal to that,               (means: info changed)
           * then record the difference
           * if the recorded difference is NOT valid,
           * then record the difference as the default value
           */
          if ( thi.isCalculated() && ! that.isConcrete() ) 
             continue;
          else if ( ( thi.isNull() && that.isNotNull() ) ||
                    ( thi.isNotNull() && that.isNotNull() ) &&
                    ! thi.getValue().equals( that.getValue() ) ) {
             diffs.set( that.getPropertyNum(), that.getValue() );
//              System.out.println( "Recording difference for datum: " + that.getColumnName() + " = " + that.getDatum() );

//             if ( diffs.getDatum( that.getPropertyNum() ).isNull() )
//                diffs.set( that.getPropertyNum(), that.getBlankValue() );
             diffsExists = true;
          }
       }
       
       // by default, a cropID of -1 means no differences.
       if ( diffsExists ) {
          System.out.println("Differences EXIST: " + diffs.toString() );
          if ( ! this.isSingleRecord() )
             diffs.setID( 1 );
          else
             diffs.setID( this.getID() );
       }
       
       return diffs;
    }

   
   public abstract List<Integer> getListOfInheritableProperties();
   public CPSRecord inheritFrom( CPSRecord thatRecord ) { 
       
       if ( thatRecord.getID() != -1 ){ 
       
           CPSDatum thisDat, thatDat;
       
           for ( Integer i : getListOfInheritableProperties() ) {
          
               int prop = i.intValue();
               thisDat = this.getDatum( prop );
               thatDat = thatRecord.getDatum( prop );
                       
//          System.out.print("DEBUG Inheriting " + thisDat.getName() );

              /* IF: this IS valid
               * THEN: ignore this datum, no inheritance needed */
               if ( thisDat.isConcrete() || thatDat == null ) {
//                   System.out.println( " SKIPPED" );
                   continue;
               }
               /* IF: this IS NOT valid AND that IS valid
                * THEN: this datum will be inherited */
               else if ( ( thisDat.isNull() || thisDat.isInherited() ) && thatDat.isNotNull() ) {
//                   System.out.println( " DONE" );
                   this.inherit( prop, thatDat.getValue() );
//                   updateCalculations( prop );
               }
//               else
//                   System.out.println( " SKIPPED FOR OTHER REASONS" );
           }
       }
       
       return this;
    }

   /**
    * Merge the values from another record into this one.  Only values which are
    * different, non-null, not calculated, etc will be merged.
    *
    * @param changes the record whose values will be considered for merger
    * @return this record, after the updates
    */
   public CPSRecord merge( CPSRecord changes ) {

       debug( "Merging records:\n" +
               this.toString() + "\n" +
               changes.toString() );

       Iterator<CPSDatum> thisIt = this.iterator();
       Iterator<CPSDatum> changeIt = changes.iterator();

       CPSDatum thisItem, changedItem;
       while ( thisIt.hasNext() && changeIt.hasNext() ) {
          thisItem = thisIt.next();
          changedItem = changeIt.next();

          /*
           * if this is CALCULATED and change IS NOT VALID, then skip
           * if this IS NOT valid AND change IS valid OR   (means: new info added)
           *    this IS     valid AND change IS valid AND
           *    this IS NOT equal to change,               (means: info changed)
           * then record the difference
           *
           * finally:
           * if the recorded difference is NOT valid,
           * then record the difference as the default value
           */
          if ( thisItem.isCalculated() && ! changedItem.isConcrete() )
             continue;
          else if ( (   thisItem.isNull() && changedItem.isNotNull() ) ||
                    (   thisItem.isNotNull() && changedItem.isNotNull() ) &&
                      ! thisItem.getValue().equals( changedItem.getValue() ) ) {
              debug( "Recording difference for datum: " +
                      this.getDatum( changedItem.getPropertyNum() ).getName() + " => " +
                      changes.getDatum( changedItem.getPropertyNum()).getValue().toString() );
              this.set( changedItem.getPropertyNum(), changedItem.getValue() );
              
//              if ( this.getDatum( changedItem.getPropertyNum() ).isNull() )
//                  this.set( changedItem.getPropertyNum(), changedItem.getBlankValue() );
             
          }
       }

       return this;
   }
   
   public boolean equals( Object o ) {
      return ( o instanceof CPSRecord && this.diff( (CPSRecord) o ).getID() == -1 );
   }
   
   public int parseInt ( String s ) {
      if ( isObjectNull(s) || s.equals("") )
         return -1;
      else
         // remove whitespace and ignore a leading '+"
         return Integer.parseInt( s.trim().replaceFirst( "^\\+", "" ));
   }
   public float parseFloat ( String s ) {
      if ( isObjectNull(s) || s.equals("") )
         return -1;
      else
         // remove whitespace and ignore a leading '+"
         return Float.parseFloat( s.trim().replaceFirst( "^\\+", "" ));
   }
   
   public <T> void inherit( int prop, T value ) {
      CPSDatum d = getDatum( prop );
      set( d, value );
      d.setInherited( true );
   }
   
   public <T> void set( int prop, T value ) { set( getDatum( prop ), value ); }
   /** 
    * Method to abstract datum setting.  Should only be used to set a real value, not a calculated or inherited value;
    * 
    * @param d datum to be set
    * @param v value to which the datum should be set
    */
   protected <T> void set( CPSDatum<T> d, T v ) {
//      debug( "Setting value of " + d.getName() + " to " + v );
      d.setInherited( false );
      d.setCalculated( false );
      d.setValue( v );
   }

   public void addChangedProperty( int prop ) {
       changedProps.add( prop );
   }
   
   public abstract Iterator iterator();
   public abstract class CPSRecordIterator implements Iterator {
       
       protected int currentProp;
       protected boolean forDisplayOnly;

       public CPSRecordIterator() { currentProp = -1; }

       public CPSRecordIterator( boolean displayOnly ) {
           this();
           forDisplayOnly = displayOnly;
       }
       
       public boolean hasNext() { return currentProp < lastValidProperty(); }

       public CPSDatum next() {

          currentProp++;
          if ( hasNext() &&
               ( ignoreThisProperty() || getDatum( currentProp ) == null ))
             return next();
          else
             return getDatum( currentProp );
          
       }

       public abstract boolean ignoreThisProperty();
       
       public void remove() {}
       
    }
}
