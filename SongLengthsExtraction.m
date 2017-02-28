function song_lengths = SongLengthsExtraction(decomp)
    black_threshold = 30;
    black_count = 0;
    
    max_number = 20;
    song_count = 0;
    song_length = 0;
    song_lengths = zeros(1, max_number);

    start_record = 0;
    
    for j = 1:size(decomp, 2)
        if (decomp(1,j) == 0)
            black_count = black_count + 1;
            if (black_count >= black_threshold)
                start_record = 1;
                black_count = 0;
            end
        end
        
        if (start_record == 1)
            if (decomp(1, j) == 255)
                if (decomp(1,j-1) == 0)
                    song_length = 0;
                end
                song_length = song_length + 1;
            elseif (decomp(1, j) == 0)
                black_count = black_count + 1;
                
                if (decomp(1, j-1) == 255)
                    if (black_count < black_threshold ...
                        && song_length > black_threshold)
                        song_count = song_count + 1;
                        song_lengths(song_count) = song_length;
                    end
                    
                    black_count = 0;
                end
            end
        end
    end
    
    song_lengths = song_lengths(song_lengths > 0);
end