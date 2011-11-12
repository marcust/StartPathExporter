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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import com.restfb.types.User;

public class FacebookExporter {

    public static void main( final String... args ) throws FileNotFoundException {
        final long initialId = 1536564580;

        final PrintWriter outputStreamWriter = new PrintWriter( new File("/tmp/facebook.txt") );
        final FacebookClient facebookClient = new DefaultFacebookClient();

        final Set<Long> seen = new HashSet<Long>();
        fetchFriends( initialId, outputStreamWriter, facebookClient, seen );

        outputStreamWriter.close();

    }

    private static void fetchFriends( final long initialId, final PrintWriter outputStreamWriter, final FacebookClient facebookClient, final Set<Long> seen ) {
        try {
            final Connection<User> fetchConnection = facebookClient.fetchConnection( initialId + "/friends", User.class);
            final Set<Long> toVisit = new HashSet<Long>();


            for ( final List<User> users : fetchConnection ) {
                for ( final User user : users ) {
                    final long friendId = Long.parseLong( user.getId() );
                    TwitterExporter.printIds( outputStreamWriter, initialId, friendId  );
                    toVisit.add( Long.valueOf( friendId ) );
                }
            }

            for ( final Long friendId :  toVisit ) {
                if ( !seen.contains( friendId ) ) {
                    seen.add( friendId );
                    fetchFriends( friendId.longValue(), outputStreamWriter, facebookClient, seen );
                }
            }

        } catch ( final FacebookException e ) {
            e.printStackTrace();
            return;
        }

    }

}
