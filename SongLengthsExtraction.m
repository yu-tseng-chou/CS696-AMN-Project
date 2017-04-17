function song_lengths = SongLengthsExtraction(decomp)
% Return vector with number of grooves in each song.
%   decomp - a 1-D array of black/white (median filtered image)

    % set minimum width to detect change of song number 
    threshold       = 15;
    song_lengths    = [];
    
    % [00011100110011000000...] decomp
    % [00111001100110000000...] shifted_decomp (<<1)
    % [00100001000100000000...] 
    %    ^    ^   ^             indices -> detected
    shifted_decomp  = [decomp(2:end) 0];
    detected        = find(decomp == 0 & shifted_decomp == 255);
    groove_count    = 1;
    
    % count number of grooves per song
    for n = 1: size(detected, 2)-1
        distance_between = detected(n+1) - detected(n);
        if  distance_between > threshold
            % found end of song
            song_lengths(numel(song_lengths)+1) = groove_count;
            groove_count        = 0;
        end
        
        groove_count     = groove_count + 1;
    end
    
    % include final song
    song_lengths(numel(song_lengths)+1) = groove_count;
end