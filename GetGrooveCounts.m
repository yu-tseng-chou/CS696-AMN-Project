function groove_counts = GetGrooveCounts(imname)

    numSubplotRows = 4;
    numSubplotCols = 1;
    autoSubplotter = MakeAutoSubplot(numSubplotRows, numSubplotCols);
    
    %% Load the original image
	im = imread(imname);
    figure
    autoSubplotter();
    imshow(im);
    title('Original Cropped Image');
    
    %% Filter the grayscale image with a high pass kernel
    G = rgb2gray(im);
    kernel = [-1 -1 -1; -1 8 -1; -1 -1 -1]/9;
    H = imfilter(single(G), kernel);
    autoSubplotter();
    imshow(H);
    title('High Pass Filter, k = [-1 -1 -1; -1  8 -1; -1 -1 -1]/9');
    
    %% Filter image with a threshold so only black and white left
    threshold = 50;
    T = im2uint8(H);
    T(T > threshold) = 255;
    T(T <= threshold) = 0;
    autoSubplotter();
    imshow(T);
    title(sprintf('Threshold Filter, \\theta = %d', threshold));
    
    %% Remove noise with Median Filter
    autoSubplotter();
    filterWidth = 3;
    singleRowDecomp = MedianDecomposition(T,filterWidth);
    % Vertically stretch decomposed row to make viewing easier
    stretchedDecmop = repmat(singleRowDecomp,size(T,1),1);
    imshow(stretchedDecmop);
    title(sprintf('Median Filter, \\omega = %d', filterWidth));
    
    %% Extract song length information
    height = size(T,1);
    groove_counts = SongLengthsExtraction(singleRowDecomp);
    
    %% Remove 2 silent grooves from each song
    groove_counts = groove_counts - 2;
    
    %% Display results
    g = sprintf('%d, ', groove_counts);
    g = g(1:size(g,2)-2); % Trim off trailing comma
    text(0, height+50, ['Groove Counts: ' g]);
end