% Clean up
clc;clear;clf;close all;

% Loading the images
filelist = dir('images/Strip*.jpg');
n = length(filelist);

for i=1:n
    %% Load the original image
    imname = filelist(i).name;
	im = imread(['images/' imname], 'jpg');
   
    figure
    subplot(4,1,1);
    imshow(im);
    title(sprintf('Original file %s', imname));
    
    %% Filter image with a median filter and convert it to gray scale
    G = rgb2gray(im);
    G = medfilt2(G);
    
    %% Get the optimal threshold for image filtering
    subplot(4,1,2);
    [counts,binLocations] = imhist(G);
    stem(binLocations, counts);
    title('Histogram of gray scale version image');
    
    % TO DO: get threshold from between peaks
    first_peak_value = 0;
    first_peak_index = 0;
    second_peak_value = 0;
    second_peak_index = 0;
    index = 0;
    for ind = 2:255
        if ((counts(ind) > first_peak_value || counts(ind) > second_peak_value) ...
            && counts(ind-1) < counts(ind) && counts(ind) > counts(ind+1))
            
            second_peak_value = counts(ind);
            second_peak_index = ind;
            if (second_peak_value > first_peak_value)
                temp_index = first_peak_index;
                temp_value = first_peak_value;
                
                first_peak_index = second_peak_index;
                first_peak_value = second_peak_value;
                
                second_peak_index = temp_index;
                second_peak_value = temp_value;
            end
        end
    end
    
    min_index = 1;
    min_value = max(counts);
    for ind = first_peak_index : sign(second_peak_index-first_peak_index): second_peak_index
        if (counts(ind) < min_value)
            min_index = ind;
            min_value = counts(ind);
        end
    end
    
    %% Filter image with a threshold so only black and white left
    threshold = min_index; % (first_peak_index + second_peak_index)/2;
    T = G;
    T(T > threshold) = 255;
    T(T <= threshold) = 0;
    subplot(4,1,3);
    imshow(T);
    title(sprintf('Filtered image with threshold = %d', threshold));
    
    %% 
    subplot(4,1,4);
    filterWidth=3;
    decomp = MedianDecomposition(T,filterWidth);
    imshow(repmat(decomp,size(T,1),1));
    title(sprintf('After median decomposition of width = %d', filterWidth));
    
    %% Extract song length information
    height = size(T,1);
    song_lengths = SongLengthsExtraction(decomp);
    g = sprintf('%d, ', song_lengths);
    g = g(1:size(g,2)-2); % Trim off trailing comma
    text(0, height+25, sprintf('%d Songs detected. Lengths: %s', size(song_lengths, 2), g));
    
end