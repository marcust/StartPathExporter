/*
 * (c) Copyright 2011 Marcus Thiesen (marcus@thiesen.org)
 *
 *  This file is part of Twitter to StarPath.
 *
 *  Twitter to StarPath is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Twitter to StarPath is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Twitter to StarPath.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.thiesen.startpath.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterExporter {

    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() + 1 );

    public static void main( final String... args ) throws TwitterException, FileNotFoundException {
        
        final PrintWriter outputStreamWriter = new PrintWriter( new File("/tmp/twitter.txt") );

        findForUser( outputStreamWriter, 20754251, new CopyOnWriteArraySet<Long>() );

    }

    private static void findForUser( final PrintWriter outputStreamWriter, final long id, final Set<Long> seen ) throws TwitterException {
        final Twitter twitter = new TwitterFactory().getInstance();
        final IDs followersIDs = twitter.getFollowersIDs( id, -1 );
        printTarget( outputStreamWriter, followersIDs, id );
        final IDs friendsIDs = twitter.getFriendsIDs( id, -1 );
        printSource( outputStreamWriter, id, friendsIDs );

        recurse( outputStreamWriter, seen, followersIDs );
        recurse( outputStreamWriter, seen, friendsIDs );
    }

    private static void recurse( final PrintWriter outputStreamWriter, final Set<Long> seen, final IDs followersIDs )
    {
        for ( final long followerId : followersIDs.getIDs() ) {
            if ( !seen.contains( Long.valueOf( followerId ))  ) {
                seen.add( Long.valueOf( followerId ) ) ;
                EXECUTOR.submit( new Runnable() {
                    @Override
                    public void run()  { 
                        try {
                            findForUser( outputStreamWriter, followerId, seen );
                        } catch ( final TwitterException e ) {
                            e.printStackTrace();
                        } 
                    }
                }
                ); 

            }
        }
    }

    private static void printSource( final PrintWriter outputStreamWriter, final long id, final IDs friendsIDs ) {
        for ( final long friendId : friendsIDs.getIDs() ) {
            printIds( outputStreamWriter, id, friendId );
        }
    }

    private static void printTarget( final PrintWriter writer, final IDs followersIDs, final long id ) {
        for ( final long followerId : followersIDs.getIDs() ) {
            printIds( writer, followerId, id );
        }
    }

    public static void printIds( final PrintWriter writer, final long source, final long target ) {
        synchronized ( writer ) {
            writer.print( "a " );
            writer.print( source );
            writer.print( ' ' );
            writer.print( target );
            writer.print( '\n' );
            writer.flush();
        }
    }

}

