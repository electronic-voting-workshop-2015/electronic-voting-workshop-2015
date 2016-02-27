require 'models/publication'

post '/publish' do
    publication = Publication.create!( content: params[ "content" ] )
    json id: publication.id
end

get '/retrieve' do
    begin
        id = params[ "id" ].to_i
        publication = Publication.find id
        json :content => publication.content
    rescue ActiveRecord::RecordNotFound => e
        raise Exception.new( "could not find id=#{id}" )
    end
end
