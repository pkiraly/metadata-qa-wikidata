# join properties.csv and properties-count.csv
library(tidyverse)

properties <- read_csv('../../../data/properties.csv')
counts <- read_csv('../../../data/properties-count.csv')

properties_with_count <- properties %>% 
  left_join(counts, by = 'id') %>% 
  filter(!is.na(count)) %>% 
  arrange(desc(count))

write_csv(properties_with_count, '../../../data/properties-with-count.csv')