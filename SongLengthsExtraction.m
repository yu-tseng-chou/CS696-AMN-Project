function song_lengths = SongLengthsExtraction(decomp)
% Return vector with number of grooves in each song.
%   decomp - a 1-D array of black/white (median filtered image)

    threshold       = 15;   % minimum width to detect change of song
    min_threshold   = 10;   % minimum width to detect final silent groove
    song_lengths    = [];
    
    % find transitions from black to white
    % 0 = black, 1 = white
    % [00011100110011000000...] decomp
    % [00111001100110000000...] shifted_decomp (<<1)
    % [00100001000100000000...] 
    %    ^    ^   ^             indices -> detected
    shifted_decomp  = [decomp(2:end) 0];
    detected        = find(decomp == 0 & shifted_decomp == 255);
    groove_count    = 1;
    
    % count number of grooves per song
    n = 1;
    while (n <= size(detected, 2)-1)
        distance_between = detected(n+1) - detected(n);
        
        % silent groove in song separator
        if groove_count == 1 && numel(song_lengths) > 0 ...
                && distance_between > min_threshold
            song_lengths(numel(song_lengths)) = ...
                song_lengths(numel(song_lengths)) + 1;
            groove_count    = 0;

        % end of song
        elseif  distance_between > threshold
            song_lengths(numel(song_lengths)+1) = groove_count;
            groove_count	= 0;
        end
        
        groove_count     = groove_count + 1;
        n                = n + 1;
    end
    
    % include final song, but not final silent groove
    if groove_count > 1
        song_lengths(numel(song_lengths)+1) = groove_count;
    end
end
